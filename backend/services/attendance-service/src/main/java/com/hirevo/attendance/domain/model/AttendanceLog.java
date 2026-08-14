package com.hirevo.attendance.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "attendance_logs", schema = "attendance")
@IdClass(AttendanceLogId.class)
public class AttendanceLog {

  @Id
  @Column(name = "id")
  private UUID id = UUID.randomUUID();

  @Id
  @Column(name = "work_date", nullable = false)
  private LocalDate workDate;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "employee_id", nullable = false)
  private UUID employeeId;

  @Column(name = "work_location_id")
  private UUID workLocationId;

  @Column(name = "clock_in_at")
  private Instant clockInAt;

  @Column(name = "clock_out_at")
  private Instant clockOutAt;

  @Column(name = "lat_in", precision = 10, scale = 7)
  private BigDecimal latIn;

  @Column(name = "lng_in", precision = 10, scale = 7)
  private BigDecimal lngIn;

  @Column(name = "lat_out", precision = 10, scale = 7)
  private BigDecimal latOut;

  @Column(name = "lng_out", precision = 10, scale = 7)
  private BigDecimal lngOut;

  @Column(name = "gps_accuracy_meters", precision = 6, scale = 2)
  private BigDecimal gpsAccuracyMeters;

  @Column(name = "is_mock_location")
  private Boolean mockLocation = false;

  @Column(name = "source_in")
  private String sourceIn;

  @Column(name = "source_out")
  private String sourceOut;

  @Column(name = "late_minutes")
  private Integer lateMinutes = 0;

  @Column(name = "worked_minutes")
  private Integer workedMinutes;

  @Column(nullable = false)
  private String status = "present";

  @Column(name = "fraud_score")
  private Integer fraudScore = 0;

  @Column(name = "is_anomaly", nullable = false)
  private boolean anomaly = false;

  @Column(name = "anomaly_reason", columnDefinition = "text")
  private String anomalyReason;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public LocalDate getWorkDate() { return workDate; }
  public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public UUID getEmployeeId() { return employeeId; }
  public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
  public UUID getWorkLocationId() { return workLocationId; }
  public void setWorkLocationId(UUID workLocationId) { this.workLocationId = workLocationId; }
  public Instant getClockInAt() { return clockInAt; }
  public void setClockInAt(Instant clockInAt) { this.clockInAt = clockInAt; }
  public Instant getClockOutAt() { return clockOutAt; }
  public void setClockOutAt(Instant clockOutAt) { this.clockOutAt = clockOutAt; }
  public BigDecimal getLatIn() { return latIn; }
  public void setLatIn(BigDecimal latIn) { this.latIn = latIn; }
  public BigDecimal getLngIn() { return lngIn; }
  public void setLngIn(BigDecimal lngIn) { this.lngIn = lngIn; }
  public BigDecimal getLatOut() { return latOut; }
  public void setLatOut(BigDecimal latOut) { this.latOut = latOut; }
  public BigDecimal getLngOut() { return lngOut; }
  public void setLngOut(BigDecimal lngOut) { this.lngOut = lngOut; }
  public BigDecimal getGpsAccuracyMeters() { return gpsAccuracyMeters; }
  public void setGpsAccuracyMeters(BigDecimal v) { this.gpsAccuracyMeters = v; }
  public Boolean getMockLocation() { return mockLocation; }
  public void setMockLocation(Boolean mockLocation) { this.mockLocation = mockLocation; }
  public String getSourceIn() { return sourceIn; }
  public void setSourceIn(String sourceIn) { this.sourceIn = sourceIn; }
  public String getSourceOut() { return sourceOut; }
  public void setSourceOut(String sourceOut) { this.sourceOut = sourceOut; }
  public Integer getLateMinutes() { return lateMinutes; }
  public void setLateMinutes(Integer lateMinutes) { this.lateMinutes = lateMinutes; }
  public Integer getWorkedMinutes() { return workedMinutes; }
  public void setWorkedMinutes(Integer workedMinutes) { this.workedMinutes = workedMinutes; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Integer getFraudScore() { return fraudScore; }
  public void setFraudScore(Integer fraudScore) { this.fraudScore = fraudScore; }
  public boolean isAnomaly() { return anomaly; }
  public void setAnomaly(boolean anomaly) { this.anomaly = anomaly; }
  public String getAnomalyReason() { return anomalyReason; }
  public void setAnomalyReason(String anomalyReason) { this.anomalyReason = anomalyReason; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
