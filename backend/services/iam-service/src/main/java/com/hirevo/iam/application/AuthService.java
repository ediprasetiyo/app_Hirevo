package com.hirevo.iam.application;

import com.hirevo.audit.Audited;
import com.hirevo.core.exception.ErrorCode;
import com.hirevo.core.exception.HirevoException;
import com.hirevo.iam.api.dto.LoginRequest;
import com.hirevo.iam.api.dto.LoginResponse;
import com.hirevo.iam.api.dto.RefreshRequest;
import com.hirevo.iam.domain.model.Permission;
import com.hirevo.iam.domain.model.Role;
import com.hirevo.iam.domain.model.Tenant;
import com.hirevo.iam.domain.model.User;
import com.hirevo.iam.domain.model.UserMfaMethod;
import com.hirevo.iam.domain.repository.TenantRepository;
import com.hirevo.iam.domain.repository.UserMfaMethodRepository;
import com.hirevo.iam.domain.repository.UserRepository;
import com.hirevo.iam.infrastructure.redis.RefreshTokenStore;
import com.hirevo.iam.infrastructure.redis.RefreshTokenStore.IssuedRefreshToken;
import com.hirevo.security.jwt.JwtProperties;
import com.hirevo.security.jwt.JwtService;
import com.hirevo.security.jwt.JwtService.AccessTokenClaims;
import com.hirevo.security.jwt.JwtService.IssuedToken;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Password login + refresh flow.
 *
 * <p>Flow:
 * <ol>
 *   <li>Resolve tenant by subdomain header (or JWT.tenant_id for refresh).</li>
 *   <li>Look up user by (tenant, email). Compare Argon2id hash.</li>
 *   <li>If MFA enabled → return challenge (202-style body, but HTTP 200 with flag).</li>
 *   <li>Else → issue access JWT + rotating refresh token.</li>
 * </ol>
 */
@Service
public class AuthService {

  private final TenantRepository tenants;
  private final UserRepository users;
  private final UserMfaMethodRepository mfaMethods;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;
  private final RefreshTokenStore refreshStore;
  private final MfaChallengeStore challengeStore;

  public AuthService(TenantRepository tenants, UserRepository users,
                     UserMfaMethodRepository mfaMethods, PasswordEncoder passwordEncoder,
                     JwtService jwtService, JwtProperties jwtProperties,
                     RefreshTokenStore refreshStore, MfaChallengeStore challengeStore) {
    this.tenants = tenants;
    this.users = users;
    this.mfaMethods = mfaMethods;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.jwtProperties = jwtProperties;
    this.refreshStore = refreshStore;
    this.challengeStore = challengeStore;
  }

  @Transactional
  @Audited(module = "auth", action = "login")
  public LoginResponse login(LoginRequest req, String tenantSubdomain) {
    Tenant tenant = tenants.findBySubdomain(tenantSubdomain)
        .orElseThrow(() -> new HirevoException(ErrorCode.TENANT_NOT_FOUND,
            "Tenant '" + tenantSubdomain + "' not found"));
    if ("suspended".equals(tenant.getStatus())) {
      throw new HirevoException(ErrorCode.TENANT_SUSPENDED, "Tenant suspended");
    }

    User user = users.findByTenantIdAndEmail(tenant.getId(), req.email().toLowerCase())
        .orElseThrow(() -> new HirevoException(ErrorCode.INVALID_CREDENTIALS,
            "Invalid credentials"));

    if (user.isLocked()) {
      throw new HirevoException(ErrorCode.ACCOUNT_LOCKED,
          "Account locked until " + user.getLockedUntil());
    }

    if (user.getPasswordHash() == null
        || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
      user.recordFailedLogin();
      users.save(user);
      throw new HirevoException(ErrorCode.INVALID_CREDENTIALS, "Invalid credentials");
    }

    // MFA gate
    if (user.isTwoFaEnabled()) {
      List<UserMfaMethod> methods = mfaMethods.findByUserIdAndRevokedAtIsNull(user.getId())
          .stream().filter(UserMfaMethod::isVerified).toList();
      if (!methods.isEmpty()) {
        String challengeId = challengeStore.createLoginChallenge(user.getId(),
            Duration.ofMinutes(5));
        List<LoginResponse.MfaMethodSummary> summaries = methods.stream()
            .map(m -> new LoginResponse.MfaMethodSummary(
                m.getId(), m.getMethodType(), m.getDisplayName()))
            .toList();
        return LoginResponse.mfaChallenge(challengeId, summaries);
      }
    }

    return issueTokens(user, req.deviceId());
  }

  @Transactional
  @Audited(module = "auth", action = "refresh")
  public LoginResponse refresh(RefreshRequest req) {
    IssuedRefreshToken rotated;
    try {
      rotated = refreshStore.rotate(req.refreshToken(), jwtProperties.getRefreshTokenTtl());
    } catch (IllegalStateException e) {
      throw new HirevoException(ErrorCode.REFRESH_TOKEN_REUSED,
          "Refresh token invalid or already used");
    }
    UUID userId = refreshStore.lookupUser(rotated.token())
        .orElseThrow(() -> new HirevoException(ErrorCode.TOKEN_INVALID, "Bad refresh state"));
    User user = users.findById(userId)
        .orElseThrow(() -> new HirevoException(ErrorCode.TOKEN_INVALID, "User no longer exists"));
    return issueTokens(user, null, rotated);
  }

  public void logout(String refreshToken) {
    refreshStore.revoke(refreshToken);
  }

  private LoginResponse issueTokens(User user, UUID deviceId) {
    IssuedRefreshToken refresh = refreshStore.issue(
        user.getId(), user.getTenantId(), deviceId, jwtProperties.getRefreshTokenTtl());
    return issueTokens(user, deviceId, refresh);
  }

  private LoginResponse issueTokens(User user, UUID deviceId, IssuedRefreshToken refresh) {
    List<String> roles = user.getRoles().stream().map(Role::getName).toList();
    List<String> permissions = user.getRoles().stream()
        .flatMap(r -> r.getPermissions().stream())
        .map(Permission::getCode)
        .distinct()
        .toList();

    AccessTokenClaims claims = new AccessTokenClaims(
        user.getId(), user.getTenantId(), null,
        roles, permissions, deviceId, UUID.randomUUID());
    IssuedToken access = jwtService.issueAccess(claims);

    user.recordSuccessfulLogin();
    users.save(user);

    long expiresIn = jwtProperties.getAccessTokenTtl().toSeconds();
    return LoginResponse.fullSuccess(
        access.token(),
        refresh.token(),
        expiresIn,
        access.expiresAt(),
        new LoginResponse.UserSummary(user.getId(), user.getEmail(), user.getFullName(), roles));
  }
}
