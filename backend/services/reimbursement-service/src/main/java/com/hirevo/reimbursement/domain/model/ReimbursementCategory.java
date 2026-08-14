package com.hirevo.reimbursement.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "reimbursement_categories")
public class ReimbursementCategory {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(name = "monthly_limit")
  private BigDecimal monthlyLimit;

  @Column(name = "yearly_limit")
  private BigDecimal yearlyLimit;

  @Column(name = "require_receipt", nullable = false)
  private boolean requireReceipt = true;

  @Column(name = "is_taxable", nullable = false)
  private boolean taxable = false;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public BigDecimal getMonthlyLimit() { return monthlyLimit; }
  public void setMonthlyLimit(BigDecimal v) { this.monthlyLimit = v; }
  public BigDecimal getYearlyLimit() { return yearlyLimit; }
  public void setYearlyLimit(BigDecimal v) { this.yearlyLimit = v; }
  public boolean isRequireReceipt() { return requireReceipt; }
  public void setRequireReceipt(boolean v) { this.requireReceipt = v; }
  public boolean isTaxable() { return taxable; }
  public void setTaxable(boolean taxable) { this.taxable = taxable; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
}
