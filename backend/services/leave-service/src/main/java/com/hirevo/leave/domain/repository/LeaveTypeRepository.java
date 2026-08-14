package com.hirevo.leave.domain.repository;

import com.hirevo.leave.domain.model.LeaveType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID> {
  List<LeaveType> findByTenantIdAndActiveTrue(UUID tenantId);
  Optional<LeaveType> findByTenantIdAndCode(UUID tenantId, String code);
  boolean existsByTenantId(UUID tenantId);
}
