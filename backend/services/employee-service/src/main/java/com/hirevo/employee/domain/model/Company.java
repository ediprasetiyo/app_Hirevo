package com.hirevo.employee.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "companies", schema = "employee")
public class Company {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "legal_name", nullable = false)
  private String legalName;

  @Column(name = "brand_name")
  private String brandName;

  private String npwp;
  private String nib;
  private String industry;

  @Column(columnDefinition = "text")
  private String address;

  @Column(name = "city_code")
  private String cityCode;

  @Column(name = "province_code")
  private String provinceCode;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @Column(name = "deleted_at")
  private Instant deletedAt;

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public String getLegalName() { return legalName; }
  public void setLegalName(String legalName) { this.legalName = legalName; }
  public String getBrandName() { return brandName; }
  public void setBrandName(String brandName) { this.brandName = brandName; }
  public String getNpwp() { return npwp; }
  public void setNpwp(String npwp) { this.npwp = npwp; }
  public String getNib() { return nib; }
  public void setNib(String nib) { this.nib = nib; }
  public String getIndustry() { return industry; }
  public void setIndustry(String industry) { this.industry = industry; }
  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }
  public String getCityCode() { return cityCode; }
  public void setCityCode(String cityCode) { this.cityCode = cityCode; }
  public String getProvinceCode() { return provinceCode; }
  public void setProvinceCode(String provinceCode) { this.provinceCode = provinceCode; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
  public Instant getDeletedAt() { return deletedAt; }
  public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
