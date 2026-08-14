package com.hirevo.leave.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "leave_types", schema = "leave_mgmt")
public class LeaveType {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(name = "is_paid", nullable = false)
  private boolean paid = true;

  @Column(name = "default_days_per_year")
  private BigDecimal defaultDaysPerYear;

  @Column(name = "carry_over_max_days")
  private BigDecimal carryOverMaxDays;

  @Column(name = "require_attachment", nullable = false)
  private boolean requireAttachment = false;

  @Column(name = "min_notice_days")
  private Integer minNoticeDays = 0;

  @Column(name = "is_system", nullable = false)
  private boolean system = false;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public boolean isPaid() { return paid; }
  public void setPaid(boolean paid) { this.paid = paid; }
  public BigDecimal getDefaultDaysPerYear() { return defaultDaysPerYear; }
  public void setDefaultDaysPerYear(BigDecimal v) { this.defaultDaysPerYear = v; }
  public BigDecimal getCarryOverMaxDays() { return carryOverMaxDays; }
  public void setCarryOverMaxDays(BigDecimal v) { this.carryOverMaxDays = v; }
  public boolean isRequireAttachment() { return requireAttachment; }
  public void setRequireAttachment(boolean v) { this.requireAttachment = v; }
  public Integer getMinNoticeDays() { return minNoticeDays; }
  public void setMinNoticeDays(Integer v) { this.minNoticeDays = v; }
  public boolean isSystem() { return system; }
  public void setSystem(boolean system) { this.system = system; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
}
