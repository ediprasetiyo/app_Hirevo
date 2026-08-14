package com.hirevo.payroll.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payroll_runs")
public class PayrollRun {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "payroll_period_id", nullable = false)
  private UUID payrollPeriodId;

  @Column(name = "company_id", nullable = false)
  private UUID companyId;

  @Column(name = "run_no")
  private String runNo;

  @Column(nullable = false, length = 20)
  private String status = "draft";

  @Column(name = "total_employees")
  private Integer totalEmployees;

  @Column(name = "total_gross")
  private BigDecimal totalGross;

  @Column(name = "total_deductions")
  private BigDecimal totalDeductions;

  @Column(name = "total_pph21")
  private BigDecimal totalPph21;

  @Column(name = "total_bpjs_employee")
  private BigDecimal totalBpjsEmployee;

  @Column(name = "total_bpjs_employer")
  private BigDecimal totalBpjsEmployer;

  @Column(name = "total_net")
  private BigDecimal totalNet;

  @Column(name = "calculated_at")
  private Instant calculatedAt;

  @Column(name = "reviewed_by")
  private UUID reviewedBy;

  @Column(name = "reviewed_at")
  private Instant reviewedAt;

  @Column(name = "approved_by")
  private UUID approvedBy;

  @Column(name = "approved_at")
  private Instant approvedAt;

  @Column(name = "paid_at")
  private Instant paidAt;

  @Column(name = "rule_pack_version", nullable = false, length = 20)
  private String rulePackVersion = "2024.1-mvp";

  @Column
  private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public UUID getPayrollPeriodId() { return payrollPeriodId; }
  public void setPayrollPeriodId(UUID v) { this.payrollPeriodId = v; }
  public UUID getCompanyId() { return companyId; }
  public void setCompanyId(UUID companyId) { this.companyId = companyId; }
  public String getRunNo() { return runNo; }
  public void setRunNo(String runNo) { this.runNo = runNo; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Integer getTotalEmployees() { return totalEmployees; }
  public void setTotalEmployees(Integer v) { this.totalEmployees = v; }
  public BigDecimal getTotalGross() { return totalGross; }
  public void setTotalGross(BigDecimal v) { this.totalGross = v; }
  public BigDecimal getTotalDeductions() { return totalDeductions; }
  public void setTotalDeductions(BigDecimal v) { this.totalDeductions = v; }
  public BigDecimal getTotalPph21() { return totalPph21; }
  public void setTotalPph21(BigDecimal v) { this.totalPph21 = v; }
  public BigDecimal getTotalBpjsEmployee() { return totalBpjsEmployee; }
  public void setTotalBpjsEmployee(BigDecimal v) { this.totalBpjsEmployee = v; }
  public BigDecimal getTotalBpjsEmployer() { return totalBpjsEmployer; }
  public void setTotalBpjsEmployer(BigDecimal v) { this.totalBpjsEmployer = v; }
  public BigDecimal getTotalNet() { return totalNet; }
  public void setTotalNet(BigDecimal v) { this.totalNet = v; }
  public Instant getCalculatedAt() { return calculatedAt; }
  public void setCalculatedAt(Instant v) { this.calculatedAt = v; }
  public UUID getReviewedBy() { return reviewedBy; }
  public void setReviewedBy(UUID v) { this.reviewedBy = v; }
  public Instant getReviewedAt() { return reviewedAt; }
  public void setReviewedAt(Instant v) { this.reviewedAt = v; }
  public UUID getApprovedBy() { return approvedBy; }
  public void setApprovedBy(UUID v) { this.approvedBy = v; }
  public Instant getApprovedAt() { return approvedAt; }
  public void setApprovedAt(Instant v) { this.approvedAt = v; }
  public Instant getPaidAt() { return paidAt; }
  public void setPaidAt(Instant v) { this.paidAt = v; }
  public String getRulePackVersion() { return rulePackVersion; }
  public void setRulePackVersion(String v) { this.rulePackVersion = v; }
  public String getNotes() { return notes; }
  public void setNotes(String notes) { this.notes = notes; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant v) { this.updatedAt = v; }
}
