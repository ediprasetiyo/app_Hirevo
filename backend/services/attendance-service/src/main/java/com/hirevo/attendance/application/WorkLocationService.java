package com.hirevo.attendance.application;

import com.hirevo.attendance.api.dto.WorkLocationRequest;
import com.hirevo.attendance.api.dto.WorkLocationResponse;
import com.hirevo.attendance.domain.model.WorkLocation;
import com.hirevo.attendance.domain.repository.WorkLocationRepository;
import com.hirevo.audit.Audited;
import com.hirevo.tenant.TenantContext;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkLocationService {

  private final WorkLocationRepository repo;

  public WorkLocationService(WorkLocationRepository repo) {
    this.repo = repo;
  }

  @Transactional
  @Audited(module = "attendance", action = "create_work_location", entityType = "work_location",
           entityIdExpression = "#result.id()")
  public WorkLocationResponse create(WorkLocationRequest req) {
    WorkLocation loc = new WorkLocation();
    loc.setTenantId(TenantContext.getRequired());
    loc.setName(req.name());
    loc.setAddress(req.address());
    loc.setLatitude(req.latitude());
    loc.setLongitude(req.longitude());
    loc.setRadiusMeters(req.radiusMeters() == null ? 100 : req.radiusMeters());
    repo.save(loc);
    return toResponse(loc);
  }

  @Transactional(readOnly = true)
  public List<WorkLocationResponse> list() {
    return repo.findByActiveTrue().stream().map(this::toResponse).toList();
  }

  private WorkLocationResponse toResponse(WorkLocation l) {
    return new WorkLocationResponse(
        l.getId(), l.getName(), l.getAddress(), l.getLatitude(), l.getLongitude(),
        l.getRadiusMeters(), l.isActive());
  }
}
