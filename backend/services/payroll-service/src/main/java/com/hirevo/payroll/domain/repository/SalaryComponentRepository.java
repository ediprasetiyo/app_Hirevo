package com.hirevo.payroll.domain.repository;

import com.hirevo.payroll.domain.model.SalaryComponent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, UUID> {
  boolean existsByTenantId(UUID tenantId);
  List<SalaryComponent> findByTenantIdAndActiveTrueOrderByDisplayOrder(UUID tenantId);
}
