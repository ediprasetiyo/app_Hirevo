package com.hirevo.iam.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tenants", schema = "tenant")
public class Tenant {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String subdomain;

  @Column(nullable = false, length = 20)
  private String plan = "free";

  @Column(nullable = false, length = 20)
  private String status = "trial";

  @Column(name = "trial_ends_at")
  private LocalDate trialEndsAt;

  @Column(name = "billing_email")
  private String billingEmail;

  private String npwp;

  @Column(columnDefinition = "text")
  private String address;

  @Column(name = "logo_url", columnDefinition = "text")
  private String logoUrl;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @Column(name = "deleted_at")
  private Instant deletedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getSubdomain() { return subdomain; }
  public void setSubdomain(String subdomain) { this.subdomain = subdomain; }
  public String getPlan() { return plan; }
  public void setPlan(String plan) { this.plan = plan; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public LocalDate getTrialEndsAt() { return trialEndsAt; }
  public void setTrialEndsAt(LocalDate trialEndsAt) { this.trialEndsAt = trialEndsAt; }
  public String getBillingEmail() { return billingEmail; }
  public void setBillingEmail(String billingEmail) { this.billingEmail = billingEmail; }
  public String getNpwp() { return npwp; }
  public void setNpwp(String npwp) { this.npwp = npwp; }
  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }
  public String getLogoUrl() { return logoUrl; }
  public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
  public Instant getDeletedAt() { return deletedAt; }
  public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
