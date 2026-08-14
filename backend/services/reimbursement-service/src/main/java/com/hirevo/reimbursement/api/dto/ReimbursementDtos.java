package com.hirevo.reimbursement.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ReimbursementDtos {
  private ReimbursementDtos() {}

  public record CategoryResponse(
      UUID id, String code, String name, BigDecimal monthlyLimit, BigDecimal yearlyLimit,
      boolean requireReceipt, boolean taxable) {}

  public record ItemInput(
      @NotNull UUID categoryId, @NotNull LocalDate transactionDate,
      String description, @NotNull BigDecimal amount) {}

  public record ItemResponse(
      UUID id, UUID categoryId, String categoryName, LocalDate transactionDate,
      String description, BigDecimal amount) {}

  public record CreateRequest(
      @NotNull UUID employeeId, @NotBlank String title, String description,
      @NotEmpty @Valid List<ItemInput> items) {}

  public record RequestResponse(
      UUID id, UUID employeeId, String requestNo, String title, String description,
      BigDecimal totalAmount, String status, List<ItemResponse> items,
      Instant paidAt, Instant createdAt) {}
}
