package com.hirevo.security.totp;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

/**
 * TOTP (RFC 6238) generator + verifier. 6 digits, 30s window, SHA-1 (Google Authenticator compat).
 */
@Service
public class TotpService {

  private static final String ALGORITHM = "HmacSHA1";
  private static final int SECRET_BYTES = 20;
  private static final int SKEW_STEPS = 1; // ±30s tolerance for clock drift
  private final SecureRandom random = new SecureRandom();
  private final TimeBasedOneTimePasswordGenerator totp;

  public TotpService() {
    try {
      this.totp = new TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(30), 6, ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("HmacSHA1 not available", e);
    }
  }

  /** Generates a fresh Base32-encoded secret (returned to user via QR only, then encrypted). */
  public String generateSecret() {
    byte[] bytes = new byte[SECRET_BYTES];
    random.nextBytes(bytes);
    return new Base32().encodeAsString(bytes).replace("=", "");
  }

  /** otpauth:// URI for QR code (Google Authenticator / Authy). */
  public String otpAuthUri(String issuer, String accountEmail, String secretBase32) {
    String label = URLEncoder.encode(issuer + ":" + accountEmail, StandardCharsets.UTF_8);
    String iss = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
    return "otpauth://totp/" + label + "?secret=" + secretBase32
        + "&issuer=" + iss + "&algorithm=SHA1&digits=6&period=30";
  }

  /** True if the presented 6-digit code matches within ±SKEW_STEPS windows. */
  public boolean verify(String secretBase32, String code) {
    if (code == null || !code.matches("\\d{6}")) return false;
    try {
      byte[] keyBytes = new Base32().decode(secretBase32);
      SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);
      int presented = Integer.parseInt(code);
      Instant now = Instant.now();
      for (int step = -SKEW_STEPS; step <= SKEW_STEPS; step++) {
        Instant window = now.plusSeconds(step * 30L);
        int expected = totp.generateOneTimePassword(key, window);
        if (constantTimeEquals(expected, presented)) return true;
      }
      return false;
    } catch (InvalidKeyException e) {
      return false;
    }
  }

  private static boolean constantTimeEquals(int a, int b) {
    int diff = a ^ b;
    return diff == 0;
  }

  /** For test/mocking: return the current 6-digit code (do NOT expose via API). */
  public String currentCode(String secretBase32) {
    try {
      byte[] keyBytes = new Base32().decode(secretBase32);
      SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);
      return String.format("%06d", totp.generateOneTimePassword(key, Instant.now()));
    } catch (InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
  }
}
