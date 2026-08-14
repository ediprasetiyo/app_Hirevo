package com.hirevo.payroll.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "salary_components")
public class SalaryComponent {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, length = 20)
  private String category;

  @Column(nullable = false, length = 20)
  private String type = "fixed";

  @Column(name = "default_amount")
  private BigDecimal defaultAmount = BigDecimal.ZERO;

  @Column(name = "is_taxable", nullable = false)
  private boolean taxable = true;

  @Column(name = "is_bpjs_kes_base", nullable = false)
  private boolean bpjsKesBase = false;

  @Column(name = "is_bpjs_tk_base", nullable = false)
  private boolean bpjsTkBase = false;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Column(name = "display_order")
  private Integer displayOrder = 0;

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public BigDecimal getDefaultAmount() { return defaultAmount; }
  public void setDefaultAmount(BigDecimal defaultAmount) { this.defaultAmount = defaultAmount; }
  public boolean isTaxable() { return taxable; }
  public void setTaxable(boolean taxable) { this.taxable = taxable; }
  public boolean isBpjsKesBase() { return bpjsKesBase; }
  public void setBpjsKesBase(boolean bpjsKesBase) { this.bpjsKesBase = bpjsKesBase; }
  public boolean isBpjsTkBase() { return bpjsTkBase; }
  public void setBpjsTkBase(boolean bpjsTkBase) { this.bpjsTkBase = bpjsTkBase; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
  public Integer getDisplayOrder() { return displayOrder; }
  public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
