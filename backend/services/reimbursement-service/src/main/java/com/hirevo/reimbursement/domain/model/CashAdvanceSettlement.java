package com.hirevo.reimbursement.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cash_advance_settlements")
public class CashAdvanceSettlement {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "cash_advance_id", nullable = false)
  private UUID cashAdvanceId;

  @Column(name = "settled_amount", nullable = false)
  private BigDecimal settledAmount;

  @Column(name = "refund_amount", nullable = false)
  private BigDecimal refundAmount = BigDecimal.ZERO;

  @Column(name = "shortfall_amount", nullable = false)
  private BigDecimal shortfallAmount = BigDecimal.ZERO;

  @Column(name = "settled_at", nullable = false)
  private Instant settledAt = Instant.now();

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public UUID getCashAdvanceId() { return cashAdvanceId; }
  public void setCashAdvanceId(UUID v) { this.cashAdvanceId = v; }
  public BigDecimal getSettledAmount() { return settledAmount; }
  public void setSettledAmount(BigDecimal v) { this.settledAmount = v; }
  public BigDecimal getRefundAmount() { return refundAmount; }
  public void setRefundAmount(BigDecimal v) { this.refundAmount = v; }
  public BigDecimal getShortfallAmount() { return shortfallAmount; }
  public void setShortfallAmount(BigDecimal v) { this.shortfallAmount = v; }
  public Instant getSettledAt() { return settledAt; }
}
