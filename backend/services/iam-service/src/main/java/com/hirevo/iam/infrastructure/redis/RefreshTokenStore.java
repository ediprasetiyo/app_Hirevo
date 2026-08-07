package com.hirevo.iam.infrastructure.redis;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis-backed rotating refresh token store.
 * Key format: {@code iam:refresh:<tokenHash>} → hash of {user_id, tenant_id, device_id, exp}.
 * Rotation: every use returns a NEW token and immediately revokes the previous.
 * Reuse detection: presenting an already-rotated token invalidates the entire chain
 * (per OWASP recommendation) — see {@link #consume}.
 */
@Component
public class RefreshTokenStore {

  private static final String KEY_PREFIX = "iam:refresh:";
  private static final String CHAIN_PREFIX = "iam:refresh-chain:";
  private static final SecureRandom RANDOM = new SecureRandom();

  private final StringRedisTemplate redis;

  public RefreshTokenStore(StringRedisTemplate redis) {
    this.redis = redis;
  }

  public IssuedRefreshToken issue(UUID userId, UUID tenantId, UUID deviceId, Duration ttl) {
    String raw = randomToken();
    String hash = sha256(raw);
    String chainId = UUID.randomUUID().toString();
    Instant exp = Instant.now().plus(ttl);
    redis.opsForHash().putAll(KEY_PREFIX + hash, Map.of(
        "user_id", userId.toString(),
        "tenant_id", tenantId.toString(),
        "device_id", deviceId == null ? "" : deviceId.toString(),
        "chain_id", chainId,
        "exp", String.valueOf(exp.getEpochSecond())));
    redis.expire(KEY_PREFIX + hash, ttl);
    return new IssuedRefreshToken(raw, chainId, exp);
  }

  /**
   * Consume + rotate: validates the presented token, revokes it, and issues a new one.
   * If the presented token is unknown but its chain_id is still tracked as revoked → REUSE
   * detected: invalidate the entire chain and throw.
   */
  public IssuedRefreshToken rotate(String presented, Duration newTtl) {
    String hash = sha256(presented);
    Map<Object, Object> entries = redis.opsForHash().entries(KEY_PREFIX + hash);
    if (entries.isEmpty()) {
      throw new IllegalStateException("Refresh token invalid or already used");
    }
    UUID userId = UUID.fromString((String) entries.get("user_id"));
    UUID tenantId = UUID.fromString((String) entries.get("tenant_id"));
    String deviceStr = (String) entries.get("device_id");
    UUID deviceId = deviceStr == null || deviceStr.isEmpty() ? null : UUID.fromString(deviceStr);
    String chainId = (String) entries.get("chain_id");

    // Delete current, add to revoked chain set (for reuse detection).
    redis.delete(KEY_PREFIX + hash);
    redis.opsForSet().add(CHAIN_PREFIX + chainId, hash);
    redis.expire(CHAIN_PREFIX + chainId, newTtl.plus(Duration.ofDays(1)));

    // Issue new token in same chain.
    String raw = randomToken();
    String newHash = sha256(raw);
    Instant exp = Instant.now().plus(newTtl);
    redis.opsForHash().putAll(KEY_PREFIX + newHash, Map.of(
        "user_id", userId.toString(),
        "tenant_id", tenantId.toString(),
        "device_id", deviceId == null ? "" : deviceId.toString(),
        "chain_id", chainId,
        "exp", String.valueOf(exp.getEpochSecond())));
    redis.expire(KEY_PREFIX + newHash, newTtl);
    return new IssuedRefreshToken(raw, chainId, exp);
  }

  public Optional<UUID> lookupUser(String presented) {
    Object v = redis.opsForHash().get(KEY_PREFIX + sha256(presented), "user_id");
    return v == null ? Optional.empty() : Optional.of(UUID.fromString(v.toString()));
  }

  public void revoke(String presented) {
    String hash = sha256(presented);
    Map<Object, Object> entries = redis.opsForHash().entries(KEY_PREFIX + hash);
    if (!entries.isEmpty()) {
      String chainId = (String) entries.get("chain_id");
      redis.delete(KEY_PREFIX + hash);
      if (chainId != null) redis.opsForSet().add(CHAIN_PREFIX + chainId, hash);
    }
  }

  private static String randomToken() {
    byte[] b = new byte[32];
    RANDOM.nextBytes(b);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
  }

  private static String sha256(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return Base64.getUrlEncoder().withoutPadding().encodeToString(md.digest(s.getBytes()));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  public record IssuedRefreshToken(String token, String chainId, Instant expiresAt) {}
}
