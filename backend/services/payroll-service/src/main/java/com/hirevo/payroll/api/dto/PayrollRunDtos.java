package com.hirevo.payroll.api.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PayrollRunDtos {
  private PayrollRunDtos() {}

  public record CreatePayrollRunRequest(@NotNull UUID payrollPeriodId) {}

  public record PayrollRunResponse(
      UUID id, UUID payrollPeriodId, String periodName, String runNo, String status,
      Integer totalEmployees, BigDecimal totalGross, BigDecimal totalDeductions,
      BigDecimal totalPph21, BigDecimal totalBpjsEmployee, BigDecimal totalBpjsEmployer,
      BigDecimal totalNet, Instant calculatedAt, Instant approvedAt, Instant paidAt) {}
}
