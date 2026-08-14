package com.hirevo.payroll.domain.repository;

import com.hirevo.payroll.domain.model.PayrollPeriod;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, UUID> {
  List<PayrollPeriod> findByTenantIdOrderByStartDateDesc(UUID tenantId);
  Optional<PayrollPeriod> findByTenantIdAndPeriodYearAndPeriodMonthAndType(
      UUID tenantId, Integer year, Integer month, String type);
}
