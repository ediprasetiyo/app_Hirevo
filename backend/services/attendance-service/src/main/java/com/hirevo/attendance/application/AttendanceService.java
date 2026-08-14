package com.hirevo.attendance.application;

import com.hirevo.attendance.api.dto.AttendanceLogResponse;
import com.hirevo.attendance.api.dto.ClockRequest;
import com.hirevo.attendance.domain.model.AttendanceLog;
import com.hirevo.attendance.domain.model.WorkLocation;
import com.hirevo.attendance.domain.repository.AttendanceLogRepository;
import com.hirevo.attendance.domain.repository.WorkLocationRepository;
import com.hirevo.audit.Audited;
import com.hirevo.core.exception.BusinessException;
import com.hirevo.core.exception.ErrorCode;
import com.hirevo.core.exception.HirevoException;
import com.hirevo.tenant.TenantContext;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clock-in/out with geofence validation.
 *
 * <p><b>Scope note (MVP simplification):</b> the full anti-fraud design
 * (face-recognition match, on-device liveness, native mock-GPS OS flags) needs
 * a trained face-embedding model and native mobile sensor APIs — neither
 * exists in this environment. What IS real here: server-side geofence
 * distance validation (haversine) against configured work_locations, and a
 * fraud_score computed from the client-reported isMockLocation flag +
 * distance-outside-radius. Face/liveness are accepted as scaffolds only.
 */
@Service
public class AttendanceService {

  private static final ZoneId JAKARTA = ZoneId.of("Asia/Jakarta");

  private final AttendanceLogRepository logs;
  private final WorkLocationRepository locations;

  public AttendanceService(AttendanceLogRepository logs, WorkLocationRepository locations) {
    this.logs = logs;
    this.locations = locations;
  }

  @Transactional
  @Audited(module = "attendance", action = "clock_in", entityType = "attendance_log",
           entityIdExpression = "#result.id()")
  public AttendanceLogResponse clockIn(ClockRequest req) {
    UUID tenantId = TenantContext.getRequired();
    UUID employeeId = req.employeeId();
    LocalDate today = LocalDate.now(JAKARTA);

    if (logs.findByEmployeeIdAndWorkDate(employeeId, today)
        .filter(l -> l.getClockInAt() != null).isPresent()) {
      throw new HirevoException(ErrorCode.ATTENDANCE_ALREADY_CLOCKED, "Already clocked in today");
    }

    GeofenceResult geo = checkGeofence(req);

    AttendanceLog log = new AttendanceLog();
    log.setTenantId(tenantId);
    log.setEmployeeId(employeeId);
    log.setWorkDate(today);
    log.setClockInAt(Instant.now());
    log.setLatIn(req.latitude());
    log.setLngIn(req.longitude());
    log.setGpsAccuracyMeters(req.accuracyMeters());
    log.setMockLocation(req.isMockLocation());
    log.setSourceIn(req.source() == null ? "web" : req.source());
    log.setWorkLocationId(geo.nearestLocationId());

    int fraudScore = 0;
    if (req.isMockLocation()) fraudScore += 60;
    if (!geo.withinRadius()) fraudScore += 40;
    log.setFraudScore(Math.min(fraudScore, 100));
    log.setAnomaly(fraudScore >= 40);
    if (!geo.withinRadius()) {
      log.setAnomalyReason("Outside geofence radius (" + Math.round(geo.distanceMeters()) + "m from nearest location)");
    }
    log.setStatus(fraudScore >= 70 ? "pending_review" : "present");

    logs.save(log);
    return toResponse(log);
  }

  @Transactional
  @Audited(module = "attendance", action = "clock_out", entityType = "attendance_log",
           entityIdExpression = "#result.id()")
  public AttendanceLogResponse clockOut(ClockRequest req) {
    LocalDate today = LocalDate.now(JAKARTA);
    AttendanceLog log = logs.findByEmployeeIdAndWorkDate(req.employeeId(), today)
        .orElseThrow(() -> new BusinessException("No clock-in found for today — clock in first"));
    if (log.getClockOutAt() != null) {
      throw new BusinessException("Already clocked out today");
    }
    log.setClockOutAt(Instant.now());
    log.setLatOut(req.latitude());
    log.setLngOut(req.longitude());
    log.setSourceOut(req.source() == null ? "web" : req.source());
    if (log.getClockInAt() != null) {
      long minutes = Duration.between(log.getClockInAt(), log.getClockOutAt()).toMinutes();
      log.setWorkedMinutes((int) minutes);
    }
    log.setUpdatedAt(Instant.now());
    logs.save(log);
    return toResponse(log);
  }

  @Transactional(readOnly = true)
  public List<AttendanceLogResponse> listForEmployee(UUID employeeId, LocalDate from, LocalDate to) {
    return logs.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(employeeId, from, to)
        .stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<AttendanceLogResponse> listAll(LocalDate from, LocalDate to) {
    return logs.findByWorkDateBetweenOrderByWorkDateDesc(from, to)
        .stream().map(this::toResponse).toList();
  }

  private GeofenceResult checkGeofence(ClockRequest req) {
    List<WorkLocation> active = locations.findByActiveTrue();
    if (active.isEmpty()) {
      // No locations configured for this tenant yet — allow, can't geofence against nothing.
      return new GeofenceResult(null, true, 0);
    }
    WorkLocation nearest = null;
    double minDistance = Double.MAX_VALUE;
    for (WorkLocation loc : active) {
      double d = GeoUtils.distanceMeters(req.latitude(), req.longitude(), loc.getLatitude(), loc.getLongitude());
      if (d < minDistance) {
        minDistance = d;
        nearest = loc;
      }
    }
    boolean within = nearest != null && minDistance <= nearest.getRadiusMeters();
    return new GeofenceResult(nearest == null ? null : nearest.getId(), within, minDistance);
  }

  private record GeofenceResult(UUID nearestLocationId, boolean withinRadius, double distanceMeters) {}

  private AttendanceLogResponse toResponse(AttendanceLog l) {
    return new AttendanceLogResponse(
        l.getId(), l.getEmployeeId(), l.getWorkDate(), l.getClockInAt(), l.getClockOutAt(),
        l.getStatus(), l.getLateMinutes(), l.getWorkedMinutes(), l.getFraudScore(),
        l.isAnomaly(), l.getAnomalyReason());
  }
}
