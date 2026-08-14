package com.hirevo.payroll.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payroll_periods")
public class PayrollPeriod {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private String name;

  @Column(name = "period_year", nullable = false)
  private Integer periodYear;

  @Column(name = "period_month", nullable = false)
  private Integer periodMonth;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "cutoff_date", nullable = false)
  private LocalDate cutoffDate;

  @Column(name = "pay_date", nullable = false)
  private LocalDate payDate;

  @Column(nullable = false, length = 20)
  private String type = "monthly";

  @Column(nullable = false, length = 20)
  private String status = "open";

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public Integer getPeriodYear() { return periodYear; }
  public void setPeriodYear(Integer v) { this.periodYear = v; }
  public Integer getPeriodMonth() { return periodMonth; }
  public void setPeriodMonth(Integer v) { this.periodMonth = v; }
  public LocalDate getStartDate() { return startDate; }
  public void setStartDate(LocalDate v) { this.startDate = v; }
  public LocalDate getEndDate() { return endDate; }
  public void setEndDate(LocalDate v) { this.endDate = v; }
  public LocalDate getCutoffDate() { return cutoffDate; }
  public void setCutoffDate(LocalDate v) { this.cutoffDate = v; }
  public LocalDate getPayDate() { return payDate; }
  public void setPayDate(LocalDate v) { this.payDate = v; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
