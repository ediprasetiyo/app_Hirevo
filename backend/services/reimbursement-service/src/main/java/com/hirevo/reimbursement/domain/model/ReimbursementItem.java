package com.hirevo.reimbursement.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reimbursement_items")
public class ReimbursementItem {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "reimbursement_request_id", nullable = false)
  private UUID reimbursementRequestId;

  @Column(name = "category_id", nullable = false)
  private UUID categoryId;

  @Column(name = "transaction_date", nullable = false)
  private LocalDate transactionDate;

  @Column
  private String description;

  @Column(nullable = false)
  private BigDecimal amount;

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public UUID getReimbursementRequestId() { return reimbursementRequestId; }
  public void setReimbursementRequestId(UUID v) { this.reimbursementRequestId = v; }
  public UUID getCategoryId() { return categoryId; }
  public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
  public LocalDate getTransactionDate() { return transactionDate; }
  public void setTransactionDate(LocalDate v) { this.transactionDate = v; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }
}
