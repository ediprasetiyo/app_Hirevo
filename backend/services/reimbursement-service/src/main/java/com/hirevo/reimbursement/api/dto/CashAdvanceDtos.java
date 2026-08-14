package com.hirevo.reimbursement.api.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class CashAdvanceDtos {
  private CashAdvanceDtos() {}

  public record CreateCashAdvanceRequest(
      @NotNull UUID employeeId, String purpose, @NotNull BigDecimal amount, LocalDate neededDate) {}

  public record SettleCashAdvanceRequest(@NotNull BigDecimal settledAmount) {}

  public record CashAdvanceResponse(
      UUID id, UUID employeeId, String requestNo, String purpose, BigDecimal amount,
      LocalDate neededDate, String status, Instant disbursedAt, Instant settledAt, Instant createdAt) {}
}
