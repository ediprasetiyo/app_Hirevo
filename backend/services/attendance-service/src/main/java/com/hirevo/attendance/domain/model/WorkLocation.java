package com.hirevo.attendance.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "work_locations", schema = "attendance")
public class WorkLocation {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "branch_id")
  private UUID branchId;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "text")
  private String address;

  @Column(nullable = false, precision = 10, scale = 7)
  private BigDecimal latitude;

  @Column(nullable = false, precision = 10, scale = 7)
  private BigDecimal longitude;

  @Column(name = "radius_meters", nullable = false)
  private Integer radiusMeters = 100;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public UUID getBranchId() { return branchId; }
  public void setBranchId(UUID branchId) { this.branchId = branchId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }
  public BigDecimal getLatitude() { return latitude; }
  public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
  public BigDecimal getLongitude() { return longitude; }
  public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
  public Integer getRadiusMeters() { return radiusMeters; }
  public void setRadiusMeters(Integer radiusMeters) { this.radiusMeters = radiusMeters; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
}
