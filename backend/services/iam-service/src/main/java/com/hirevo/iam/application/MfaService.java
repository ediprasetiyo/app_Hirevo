package com.hirevo.iam.application;

import com.hirevo.audit.Audited;
import com.hirevo.core.exception.ErrorCode;
import com.hirevo.core.exception.HirevoException;
import com.hirevo.iam.api.dto.LoginResponse;
import com.hirevo.iam.api.dto.MfaVerifyRequest;
import com.hirevo.iam.domain.model.User;
import com.hirevo.iam.domain.model.UserMfaMethod;
import com.hirevo.iam.domain.repository.UserMfaMethodRepository;
import com.hirevo.iam.domain.repository.UserRepository;
import com.hirevo.iam.infrastructure.crypto.FieldEncryptor;
import com.hirevo.iam.infrastructure.redis.RefreshTokenStore;
import com.hirevo.security.jwt.JwtProperties;
import com.hirevo.security.jwt.JwtService;
import com.hirevo.security.totp.TotpService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enrol + verify TOTP MFA. WebAuthn enrol/verify is a future extension —
 * this class handles the TOTP path (Sprint 1 scope).
 */
@Service
public class MfaService {

  private final UserRepository users;
  private final UserMfaMethodRepository mfaRepo;
  private final TotpService totp;
  private final FieldEncryptor encryptor;
  private final MfaChallengeStore challengeStore;
  private final AuthTokenIssuer tokenIssuer;

  public MfaService(UserRepository users, UserMfaMethodRepository mfaRepo, TotpService totp,
                    FieldEncryptor encryptor, MfaChallengeStore challengeStore,
                    AuthTokenIssuer tokenIssuer) {
    this.users = users;
    this.mfaRepo = mfaRepo;
    this.totp = totp;
    this.encryptor = encryptor;
    this.challengeStore = challengeStore;
    this.tokenIssuer = tokenIssuer;
  }

  @Transactional
  @Audited(module = "auth", action = "mfa_enroll_start", entityType = "user",
           entityIdExpression = "#userId")
  public Map<String, Object> beginTotpEnrollment(UUID userId, String userEmail) {
    String secret = totp.generateSecret();
    String otpauth = totp.otpAuthUri("Hirevo", userEmail, secret);
    UserMfaMethod m = new UserMfaMethod();
    m.setUserId(userId);
    User user = users.findById(userId).orElseThrow();
    m.setTenantId(user.getTenantId());
    m.setMethodType("totp");
    m.setDisplayName("Authenticator App");
    m.setTotpSecretEncrypted(encryptor.encrypt(secret.getBytes()));
    m.setVerified(false);
    mfaRepo.save(m);
    return Map.of(
        "method_id", m.getId(),
        "secret_base32", secret, // returned once for QR display; never again
        "otpauth_uri", otpauth);
  }

  @Transactional
  @Audited(module = "auth", action = "mfa_enroll_confirm", entityType = "user_mfa_method",
           entityIdExpression = "#methodId")
  public void confirmTotpEnrollment(UUID userId, UUID methodId, String code) {
    UserMfaMethod m = mfaRepo.findById(methodId)
        .filter(x -> x.getUserId().equals(userId))
        .orElseThrow(() -> new HirevoException(ErrorCode.RESOURCE_NOT_FOUND, "MFA method not found"));
    String secret = new String(encryptor.decrypt(m.getTotpSecretEncrypted()));
    if (!totp.verify(secret, code)) {
      throw new HirevoException(ErrorCode.MFA_INVALID_CODE, "Wrong code");
    }
    m.setVerified(true);
    m.setPrimary(true);
    User user = users.findById(userId).orElseThrow();
    user.setTwoFaEnabled(true);
    users.save(user);
  }

  @Transactional
  @Audited(module = "auth", action = "mfa_verify")
  public LoginResponse verifyLoginChallenge(MfaVerifyRequest req) {
    UUID userId = challengeStore.consume(req.challengeId())
        .orElseThrow(() -> new HirevoException(ErrorCode.MFA_CHALLENGE_EXPIRED,
            "Challenge expired or already used"));
    List<UserMfaMethod> methods = mfaRepo.findByUserIdAndRevokedAtIsNull(userId).stream()
        .filter(UserMfaMethod::isVerified).toList();
    UserMfaMethod method = methods.stream()
        .filter(m -> req.methodId() == null || m.getId().equals(req.methodId()))
        .findFirst()
        .orElseThrow(() -> new HirevoException(ErrorCode.MFA_INVALID_CODE,
            "MFA method not available"));
    if (!"totp".equals(method.getMethodType())) {
      throw new HirevoException(ErrorCode.MFA_INVALID_CODE,
          "Method not supported by TOTP verifier");
    }
    String secret = new String(encryptor.decrypt(method.getTotpSecretEncrypted()));
    if (!totp.verify(secret, req.code())) {
      throw new HirevoException(ErrorCode.MFA_INVALID_CODE, "Wrong code");
    }
    method.setLastUsedAt(Instant.now());
    mfaRepo.save(method);
    return tokenIssuer.issueForUser(userId, null);
  }
}
