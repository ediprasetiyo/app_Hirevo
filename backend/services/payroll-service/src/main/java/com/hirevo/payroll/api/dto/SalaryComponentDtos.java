package com.hirevo.payroll.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.UUID;

public class SalaryComponentDtos {
  private SalaryComponentDtos() {}

  public record CreateSalaryComponentRequest(
      @NotBlank String code,
      @NotBlank String name,
      @NotBlank String category,
      BigDecimal defaultAmount,
      Boolean taxable,
      Boolean bpjsKesBase,
      Boolean bpjsTkBase) {}

  public record SalaryComponentResponse(
      UUID id, String code, String name, String category, String type,
      BigDecimal defaultAmount, boolean taxable, boolean bpjsKesBase, boolean bpjsTkBase) {}
}
