package com.hirevo.leave.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "leave_balances", schema = "leave_mgmt")
public class LeaveBalance {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "employee_id", nullable = false)
  private UUID employeeId;

  @Column(name = "leave_type_id", nullable = false)
  private UUID leaveTypeId;

  @Column(nullable = false)
  private Integer year;

  @Column(name = "initial_balance", nullable = false)
  private BigDecimal initialBalance;

  @Column(name = "carry_over", nullable = false)
  private BigDecimal carryOver = BigDecimal.ZERO;

  @Column(nullable = false)
  private BigDecimal used = BigDecimal.ZERO;

  @Column(nullable = false)
  private BigDecimal pending = BigDecimal.ZERO;

  // `remaining` is a Postgres GENERATED ALWAYS AS column — read-only from JPA's
  // perspective; map it @Formula-less as insertable=false/updatable=false so
  // Hibernate never tries to write it (that would fail: Postgres rejects any
  // INSERT/UPDATE touching a generated column).
  @Column(name = "remaining", insertable = false, updatable = false)
  private BigDecimal remaining;

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public UUID getEmployeeId() { return employeeId; }
  public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
  public UUID getLeaveTypeId() { return leaveTypeId; }
  public void setLeaveTypeId(UUID leaveTypeId) { this.leaveTypeId = leaveTypeId; }
  public Integer getYear() { return year; }
  public void setYear(Integer year) { this.year = year; }
  public BigDecimal getInitialBalance() { return initialBalance; }
  public void setInitialBalance(BigDecimal v) { this.initialBalance = v; }
  public BigDecimal getCarryOver() { return carryOver; }
  public void setCarryOver(BigDecimal v) { this.carryOver = v; }
  public BigDecimal getUsed() { return used; }
  public void setUsed(BigDecimal used) { this.used = used; }
  public BigDecimal getPending() { return pending; }
  public void setPending(BigDecimal pending) { this.pending = pending; }
  public BigDecimal getRemaining() { return remaining; }
}
