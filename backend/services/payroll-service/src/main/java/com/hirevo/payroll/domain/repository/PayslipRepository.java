package com.hirevo.payroll.domain.repository;

import com.hirevo.payroll.domain.model.Payslip;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayslipRepository extends JpaRepository<Payslip, UUID> {
  List<Payslip> findByPayrollRunId(UUID payrollRunId);
  List<Payslip> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);
  void deleteByPayrollRunId(UUID payrollRunId);
}
