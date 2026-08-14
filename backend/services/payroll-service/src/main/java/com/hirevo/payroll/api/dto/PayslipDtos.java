package com.hirevo.payroll.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PayslipDtos {
  private PayslipDtos() {}

  public record PayslipResponse(
      UUID id, UUID payrollRunId, UUID employeeId, String employeeName,
      BigDecimal grossAmount, BigDecimal taxableAmount, BigDecimal pph21Amount,
      BigDecimal bpjsEmployee, BigDecimal bpjsEmployer, BigDecimal otherDeductions,
      BigDecimal netAmount, String ptkpCode, String terCategory,
      String calculationSnapshot, Instant createdAt) {}
}
