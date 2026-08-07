package com.hirevo.security.jwt;

import java.util.List;
import java.util.UUID;

/** Immutable principal snapshot derived from a validated JWT. */
public record HirevoPrincipal(
    UUID userId,
    UUID tenantId,
    UUID employeeId,
    List<String> roles,
    List<String> permissions,
    UUID deviceId,
    UUID sessionId) {

  public HirevoPrincipal(JwtService.ParsedClaims c) {
    this(c.userId(), c.tenantId(), c.employeeId(), c.roles(), c.permissions(),
         c.deviceId(), c.sessionId());
  }

  public boolean hasPermission(String permission) {
    return permissions.contains(permission);
  }

  public boolean hasRole(String role) {
    return roles.contains(role);
  }
}
