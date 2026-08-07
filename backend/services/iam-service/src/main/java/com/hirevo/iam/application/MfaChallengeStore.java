package com.hirevo.iam.application;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Short-lived MFA challenge state stored in Redis. */
@Component
public class MfaChallengeStore {

  private static final String KEY = "iam:mfa:challenge:";

  private final StringRedisTemplate redis;

  public MfaChallengeStore(StringRedisTemplate redis) {
    this.redis = redis;
  }

  public String createLoginChallenge(UUID userId, Duration ttl) {
    String id = UUID.randomUUID().toString();
    redis.opsForValue().set(KEY + id, userId.toString(), ttl);
    return id;
  }

  public Optional<UUID> consume(String challengeId) {
    String v = redis.opsForValue().getAndDelete(KEY + challengeId);
    return v == null ? Optional.empty() : Optional.of(UUID.fromString(v));
  }
}
