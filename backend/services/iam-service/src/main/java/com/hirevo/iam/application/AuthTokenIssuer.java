package com.hirevo.iam.application;

import com.hirevo.iam.api.dto.LoginResponse;
import com.hirevo.iam.domain.model.Permission;
import com.hirevo.iam.domain.model.Role;
import com.hirevo.iam.domain.model.User;
import com.hirevo.iam.domain.repository.UserRepository;
import com.hirevo.iam.infrastructure.redis.RefreshTokenStore;
import com.hirevo.security.jwt.JwtProperties;
import com.hirevo.security.jwt.JwtService;
import com.hirevo.security.jwt.JwtService.AccessTokenClaims;
import com.hirevo.security.jwt.JwtService.IssuedToken;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Shared helper used by both AuthService and MfaService to mint tokens for a user. */
@Component
public class AuthTokenIssuer {

  private final UserRepository users;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;
  private final RefreshTokenStore refreshStore;

  public AuthTokenIssuer(UserRepository users, JwtService jwtService,
                         JwtProperties jwtProperties, RefreshTokenStore refreshStore) {
    this.users = users;
    this.jwtService = jwtService;
    this.jwtProperties = jwtProperties;
    this.refreshStore = refreshStore;
  }

  public LoginResponse issueForUser(UUID userId, UUID deviceId) {
    User user = users.findById(userId).orElseThrow();
    List<String> roles = user.getRoles().stream().map(Role::getName).toList();
    List<String> permissions = user.getRoles().stream()
        .flatMap(r -> r.getPermissions().stream())
        .map(Permission::getCode)
        .distinct().toList();
    var refresh = refreshStore.issue(
        user.getId(), user.getTenantId(), deviceId, jwtProperties.getRefreshTokenTtl());
    IssuedToken access = jwtService.issueAccess(new AccessTokenClaims(
        user.getId(), user.getTenantId(), null,
        roles, permissions, deviceId, UUID.randomUUID()));
    user.recordSuccessfulLogin();
    users.save(user);
    return LoginResponse.fullSuccess(
        access.token(), refresh.token(),
        jwtProperties.getAccessTokenTtl().toSeconds(),
        access.expiresAt(),
        new LoginResponse.UserSummary(user.getId(), user.getEmail(), user.getFullName(), roles));
  }
}
