package com.hirevo.iam.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_mfa_methods", schema = "iam")
public class UserMfaMethod {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "method_type", nullable = false, length = 20)
  private String methodType; // 'totp' | 'webauthn' | 'sms' | 'email'

  @Column(name = "display_name")
  private String displayName;

  /** TOTP secret — encrypted at-rest via envelope encryption (KMS-wrapped DEK). */
  @Column(name = "totp_secret_encrypted", columnDefinition = "bytea")
  private byte[] totpSecretEncrypted;

  @Column(name = "is_primary", nullable = false)
  private boolean primary = false;

  @Column(name = "is_verified", nullable = false)
  private boolean verified = false;

  @Column(name = "last_used_at")
  private Instant lastUsedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "revoked_at")
  private Instant revokedAt;

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
  public String getMethodType() { return methodType; }
  public void setMethodType(String methodType) { this.methodType = methodType; }
  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }
  public byte[] getTotpSecretEncrypted() { return totpSecretEncrypted; }
  public void setTotpSecretEncrypted(byte[] v) { this.totpSecretEncrypted = v; }
  public boolean isPrimary() { return primary; }
  public void setPrimary(boolean primary) { this.primary = primary; }
  public boolean isVerified() { return verified; }
  public void setVerified(boolean verified) { this.verified = verified; }
  public Instant getLastUsedAt() { return lastUsedAt; }
  public void setLastUsedAt(Instant t) { this.lastUsedAt = t; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getRevokedAt() { return revokedAt; }
  public void setRevokedAt(Instant t) { this.revokedAt = t; }
}
