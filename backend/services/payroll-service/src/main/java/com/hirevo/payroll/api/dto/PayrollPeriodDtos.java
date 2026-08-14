package com.hirevo.payroll.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public class PayrollPeriodDtos {
  private PayrollPeriodDtos() {}

  public record CreatePayrollPeriodRequest(
      @NotNull Integer year, @NotNull Integer month, String type) {}

  public record PayrollPeriodResponse(
      UUID id, String name, int periodYear, int periodMonth,
      LocalDate startDate, LocalDate endDate, LocalDate cutoffDate, LocalDate payDate,
      String type, String status) {}
}
