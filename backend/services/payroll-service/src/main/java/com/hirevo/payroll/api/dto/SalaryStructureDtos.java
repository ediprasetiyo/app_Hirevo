package com.hirevo.payroll.api.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class SalaryStructureDtos {
  private SalaryStructureDtos() {}

  public record CreateSalaryStructureRequest(
      @NotNull UUID employeeId,
      @NotNull UUID salaryComponentId,
      @NotNull BigDecimal amount,
      @NotNull LocalDate effectiveFrom,
      String reason) {}

  public record SalaryStructureResponse(
      UUID id, UUID employeeId, UUID salaryComponentId, String salaryComponentName,
      String category, BigDecimal amount, LocalDate effectiveFrom, LocalDate effectiveTo) {}
}
