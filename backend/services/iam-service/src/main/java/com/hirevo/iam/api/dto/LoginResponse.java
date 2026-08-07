package com.hirevo.iam.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    Instant accessExpiresAt,
    String tokenType,
    UserSummary user,
    Boolean mfaRequired,
    String challengeId,
    List<MfaMethodSummary> availableMfaMethods) {

  public static LoginResponse fullSuccess(String access, String refresh, long expiresIn,
                                          Instant exp, UserSummary user) {
    return new LoginResponse(access, refresh, expiresIn, exp, "Bearer", user, false, null, null);
  }

  public static LoginResponse mfaChallenge(String challengeId, List<MfaMethodSummary> methods) {
    return new LoginResponse(null, null, 0, null, null, null, true, challengeId, methods);
  }

  public record UserSummary(UUID id, String email, String fullName, List<String> roles) {}
  public record MfaMethodSummary(UUID id, String type, String displayName) {}
}
