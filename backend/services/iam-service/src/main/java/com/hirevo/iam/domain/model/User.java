package com.hirevo.iam.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "iam",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "email"}))
public class User {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private String email;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  private String phone;

  @Column(name = "avatar_url", columnDefinition = "text")
  private String avatarUrl;

  @Column(nullable = false, length = 20)
  private String status = "invited";

  @Column(name = "two_fa_enabled", nullable = false)
  private boolean twoFaEnabled = false;

  @Column(name = "last_login_at")
  private Instant lastLoginAt;

  @Column(name = "failed_logins", nullable = false)
  private int failedLogins = 0;

  @Column(name = "locked_until")
  private Instant lockedUntil;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles", schema = "iam",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  public boolean isLocked() {
    return lockedUntil != null && lockedUntil.isAfter(Instant.now());
  }

  public void recordSuccessfulLogin() {
    this.failedLogins = 0;
    this.lockedUntil = null;
    this.lastLoginAt = Instant.now();
  }

  public void recordFailedLogin() {
    this.failedLogins++;
    if (this.failedLogins >= 5) {
      this.lockedUntil = Instant.now().plusSeconds(900); // 15 min
    }
  }

  // getters/setters
  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }
  public String getAvatarUrl() { return avatarUrl; }
  public void setAvatarUrl(String url) { this.avatarUrl = url; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public boolean isTwoFaEnabled() { return twoFaEnabled; }
  public void setTwoFaEnabled(boolean twoFaEnabled) { this.twoFaEnabled = twoFaEnabled; }
  public Instant getLastLoginAt() { return lastLoginAt; }
  public int getFailedLogins() { return failedLogins; }
  public Instant getLockedUntil() { return lockedUntil; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
  public Instant getDeletedAt() { return deletedAt; }
  public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
  public Set<Role> getRoles() { return roles; }
  public void setRoles(Set<Role> roles) { this.roles = roles; }
}
