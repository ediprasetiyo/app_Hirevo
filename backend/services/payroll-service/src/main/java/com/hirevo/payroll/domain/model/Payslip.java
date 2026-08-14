package com.hirevo.payroll.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Per-employee payslip for one payroll run.
 *
 * <p>The schema also defines a partitioned {@code payslip_lines} table for a
 * fully itemized breakdown (one row per salary component). This MVP skips
 * that table — it would require a composite {@code @IdClass} keyed on the
 * partition column ({@code created_at}), same as attendance-service's
 * {@code AttendanceLog}, for a feature (per-line audit history across
 * payroll periods) not needed yet. Instead the itemized breakdown is stored
 * as a JSON snapshot in {@code calculation_snapshot} on this row, which the
 * schema already provisions for exactly this purpose.
 */
@Entity
@Table(name = "payslips")
public class Payslip {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "payroll_run_id", nullable = false)
  private UUID payrollRunId;

  @Column(name = "employee_id", nullable = false)
  private UUID employeeId;

  @Column(name = "gross_amount", nullable = false)
  private BigDecimal grossAmount;

  @Column(name = "taxable_amount", nullable = false)
  private BigDecimal taxableAmount;

  @Column(name = "pph21_amount", nullable = false)
  private BigDecimal pph21Amount = BigDecimal.ZERO;

  @Column(name = "bpjs_employee", nullable = false)
  private BigDecimal bpjsEmployee = BigDecimal.ZERO;

  @Column(name = "bpjs_employer", nullable = false)
  private BigDecimal bpjsEmployer = BigDecimal.ZERO;

  @Column(name = "other_deductions", nullable = false)
  private BigDecimal otherDeductions = BigDecimal.ZERO;

  @Column(name = "net_amount", nullable = false)
  private BigDecimal netAmount;

  @Column(name = "payment_method", length = 20)
  private String paymentMethod = "bank_transfer";

  @Column(name = "is_sent", nullable = false)
  private boolean sent = false;

  @Column(name = "ptkp_code", length = 10)
  private String ptkpCode;

  @Column(name = "ter_category", length = 5)
  private String terCategory;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "calculation_snapshot", columnDefinition = "jsonb")
  private String calculationSnapshot;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public UUID getPayrollRunId() { return payrollRunId; }
  public void setPayrollRunId(UUID v) { this.payrollRunId = v; }
  public UUID getEmployeeId() { return employeeId; }
  public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
  public BigDecimal getGrossAmount() { return grossAmount; }
  public void setGrossAmount(BigDecimal v) { this.grossAmount = v; }
  public BigDecimal getTaxableAmount() { return taxableAmount; }
  public void setTaxableAmount(BigDecimal v) { this.taxableAmount = v; }
  public BigDecimal getPph21Amount() { return pph21Amount; }
  public void setPph21Amount(BigDecimal v) { this.pph21Amount = v; }
  public BigDecimal getBpjsEmployee() { return bpjsEmployee; }
  public void setBpjsEmployee(BigDecimal v) { this.bpjsEmployee = v; }
  public BigDecimal getBpjsEmployer() { return bpjsEmployer; }
  public void setBpjsEmployer(BigDecimal v) { this.bpjsEmployer = v; }
  public BigDecimal getOtherDeductions() { return otherDeductions; }
  public void setOtherDeductions(BigDecimal v) { this.otherDeductions = v; }
  public BigDecimal getNetAmount() { return netAmount; }
  public void setNetAmount(BigDecimal v) { this.netAmount = v; }
  public String getPaymentMethod() { return paymentMethod; }
  public void setPaymentMethod(String v) { this.paymentMethod = v; }
  public boolean isSent() { return sent; }
  public void setSent(boolean sent) { this.sent = sent; }
  public String getPtkpCode() { return ptkpCode; }
  public void setPtkpCode(String ptkpCode) { this.ptkpCode = ptkpCode; }
  public String getTerCategory() { return terCategory; }
  public void setTerCategory(String terCategory) { this.terCategory = terCategory; }
  public String getCalculationSnapshot() { return calculationSnapshot; }
  public void setCalculationSnapshot(String v) { this.calculationSnapshot = v; }
  public Instant getCreatedAt() { return createdAt; }
}
