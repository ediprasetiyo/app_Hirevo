package com.hirevo.payroll.domain.repository;

import com.hirevo.payroll.domain.model.PayrollRun;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, UUID> {
  List<PayrollRun> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
  List<PayrollRun> findByPayrollPeriodIdOrderByCreatedAtDesc(UUID payrollPeriodId);
}
