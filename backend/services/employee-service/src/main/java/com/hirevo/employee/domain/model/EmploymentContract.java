package com.hirevo.employee.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employment_contracts", schema = "employee")
public class EmploymentContract {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "employee_id", nullable = false)
  private UUID employeeId;

  @Column(name = "company_id", nullable = false)
  private UUID companyId;

  @Column(name = "branch_id")
  private UUID branchId;

  @Column(name = "position_id")
  private UUID positionId;

  @Column(name = "contract_no")
  private String contractNo;

  @Column(name = "contract_type", nullable = false)
  private String contractType;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "probation_until")
  private LocalDate probationUntil;

  @Column(name = "base_salary", nullable = false)
  private BigDecimal baseSalary;

  @Column(name = "work_arrangement")
  private String workArrangement;

  @Column(nullable = false)
  private String status = "active";

  @Column(name = "document_url", columnDefinition = "text")
  private String documentUrl;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public UUID getEmployeeId() { return employeeId; }
  public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
  public UUID getCompanyId() { return companyId; }
  public void setCompanyId(UUID companyId) { this.companyId = companyId; }
  public UUID getBranchId() { return branchId; }
  public void setBranchId(UUID branchId) { this.branchId = branchId; }
  public UUID getPositionId() { return positionId; }
  public void setPositionId(UUID positionId) { this.positionId = positionId; }
  public String getContractNo() { return contractNo; }
  public void setContractNo(String contractNo) { this.contractNo = contractNo; }
  public String getContractType() { return contractType; }
  public void setContractType(String contractType) { this.contractType = contractType; }
  public LocalDate getStartDate() { return startDate; }
  public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
  public LocalDate getEndDate() { return endDate; }
  public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
  public LocalDate getProbationUntil() { return probationUntil; }
  public void setProbationUntil(LocalDate probationUntil) { this.probationUntil = probationUntil; }
  public BigDecimal getBaseSalary() { return baseSalary; }
  public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
  public String getWorkArrangement() { return workArrangement; }
  public void setWorkArrangement(String workArrangement) { this.workArrangement = workArrangement; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getDocumentUrl() { return documentUrl; }
  public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
