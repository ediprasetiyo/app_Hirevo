package com.hirevo.security.jwt;

import com.hirevo.core.exception.ErrorCode;
import com.hirevo.core.exception.HirevoException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * Issues & validates access JWTs.
 * Refresh tokens live in Redis (see iam-service) — this class handles the access side.
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class JwtService {

  private final JwtProperties props;
  private final SecretKey signingKey;

  public JwtService(JwtProperties props) {
    this.props = props;
    // Dev/local: HMAC. Prod: swap to RSA loaded from KMS in a @Configuration bean.
    byte[] keyBytes = Decoders.BASE64URL.decode(
        java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString(props.getHmacSecret().getBytes()));
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Issue an access token with the standard Hirevo claim set.
   */
  public IssuedToken issueAccess(AccessTokenClaims claims) {
    Instant now = Instant.now();
    Instant exp = now.plus(props.getAccessTokenTtl());
    String token = Jwts.builder()
        .id(UUID.randomUUID().toString())
        .issuer(props.getIssuer())
        .subject(claims.userId().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .claim("tenant_id", claims.tenantId().toString())
        .claim("employee_id", claims.employeeId() == null ? null : claims.employeeId().toString())
        .claim("roles", claims.roles())
        .claim("permissions", claims.permissions())
        .claim("device_id", claims.deviceId() == null ? null : claims.deviceId().toString())
        .claim("session_id", claims.sessionId() == null ? null : claims.sessionId().toString())
        .signWith(signingKey)
        .compact();
    return new IssuedToken(token, exp);
  }

  /**
   * Parse & validate an access token, returning claims on success.
   * Throws HirevoException(TOKEN_EXPIRED / TOKEN_INVALID) otherwise.
   */
  public ParsedClaims parse(String token) {
    try {
      Claims c = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
      return new ParsedClaims(
          UUID.fromString(c.getSubject()),
          UUID.fromString((String) c.get("tenant_id")),
          asUuidOrNull(c.get("employee_id")),
          asStringList(c.get("roles")),
          asStringList(c.get("permissions")),
          asUuidOrNull(c.get("device_id")),
          asUuidOrNull(c.get("session_id")),
          c.getId());
    } catch (ExpiredJwtException e) {
      throw new HirevoException(ErrorCode.TOKEN_EXPIRED, "Access token expired", e);
    } catch (JwtException | IllegalArgumentException e) {
      throw new HirevoException(ErrorCode.TOKEN_INVALID, "Invalid token", e);
    }
  }

  private static UUID asUuidOrNull(Object v) {
    return v == null ? null : UUID.fromString(v.toString());
  }

  @SuppressWarnings("unchecked")
  private static List<String> asStringList(Object v) {
    return v == null ? List.of() : (List<String>) v;
  }

  public record AccessTokenClaims(
      UUID userId,
      UUID tenantId,
      UUID employeeId,
      List<String> roles,
      List<String> permissions,
      UUID deviceId,
      UUID sessionId) {}

  public record ParsedClaims(
      UUID userId,
      UUID tenantId,
      UUID employeeId,
      List<String> roles,
      List<String> permissions,
      UUID deviceId,
      UUID sessionId,
      String jti) {}

  public record IssuedToken(String token, Instant expiresAt) {
    public Map<String, Object> asMap() {
      return Map.of("token", token, "expires_at", expiresAt.toString());
    }
  }
}
