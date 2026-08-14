package com.hirevo.reimbursement.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cash_advances")
public class CashAdvance {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "employee_id", nullable = false)
  private UUID employeeId;

  @Column(name = "request_no")
  private String requestNo;

  @Column
  private String purpose;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(name = "needed_date")
  private LocalDate neededDate;

  @Column(nullable = false, length = 20)
  private String status = "pending";

  @Column(name = "disbursed_at")
  private Instant disbursedAt;

  @Column(name = "settled_at")
  private Instant settledAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public UUID getEmployeeId() { return employeeId; }
  public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
  public String getRequestNo() { return requestNo; }
  public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
  public String getPurpose() { return purpose; }
  public void setPurpose(String purpose) { this.purpose = purpose; }
  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }
  public LocalDate getNeededDate() { return neededDate; }
  public void setNeededDate(LocalDate neededDate) { this.neededDate = neededDate; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Instant getDisbursedAt() { return disbursedAt; }
  public void setDisbursedAt(Instant v) { this.disbursedAt = v; }
  public Instant getSettledAt() { return settledAt; }
  public void setSettledAt(Instant v) { this.settledAt = v; }
  public Instant getCreatedAt() { return createdAt; }
}
