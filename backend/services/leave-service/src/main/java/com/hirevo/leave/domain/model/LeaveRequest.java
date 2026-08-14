package com.hirevo.leave.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leave_requests", schema = "leave_mgmt")
public class LeaveRequest {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "employee_id", nullable = false)
  private UUID employeeId;

  @Column(name = "leave_type_id", nullable = false)
  private UUID leaveTypeId;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "total_days", nullable = false)
  private BigDecimal totalDays;

  @Column(columnDefinition = "text")
  private String reason;

  @Column(nullable = false)
  private String status = "pending";

  @Column(name = "approved_at")
  private Instant approvedAt;

  @Column(name = "cancelled_at")
  private Instant cancelledAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public UUID getEmployeeId() { return employeeId; }
  public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
  public UUID getLeaveTypeId() { return leaveTypeId; }
  public void setLeaveTypeId(UUID leaveTypeId) { this.leaveTypeId = leaveTypeId; }
  public LocalDate getStartDate() { return startDate; }
  public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
  public LocalDate getEndDate() { return endDate; }
  public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
  public BigDecimal getTotalDays() { return totalDays; }
  public void setTotalDays(BigDecimal totalDays) { this.totalDays = totalDays; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Instant getApprovedAt() { return approvedAt; }
  public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
  public Instant getCancelledAt() { return cancelledAt; }
  public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
