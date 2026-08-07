package com.hirevo.iam.infrastructure.crypto;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Field-level AES-256-GCM encryptor for sensitive columns (TOTP secret, NIK, NPWP, bank a/c).
 *
 * <p>Dev/local: uses a static key from config (fine for dev only).
 * <p>Prod: swap this bean for a KMS-backed implementation with envelope encryption
 * (per-tenant DEK wrapped by KMS KEK, cached in Redis with 5-min TTL).
 */
@Component
public class FieldEncryptor {

  private static final int GCM_IV_BYTES = 12;
  private static final int GCM_TAG_BITS = 128;
  private static final SecureRandom RANDOM = new SecureRandom();

  private final SecretKey key;

  public FieldEncryptor(
      @Value("${hirevo.security.field-encryption.key-base64:qeYqCEsBGqK9wCk8jFvXsK9lF4B0kR9wUwB3wKt5S1s=}")
      String keyBase64) {
    byte[] keyBytes = java.util.Base64.getDecoder().decode(keyBase64);
    if (keyBytes.length != 32) {
      throw new IllegalStateException("field-encryption key must be 32 bytes (AES-256)");
    }
    this.key = new SecretKeySpec(keyBytes, "AES");
  }

  public byte[] encrypt(byte[] plaintext) {
    try {
      byte[] iv = new byte[GCM_IV_BYTES];
      RANDOM.nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
      byte[] ct = cipher.doFinal(plaintext);
      byte[] out = new byte[iv.length + ct.length];
      System.arraycopy(iv, 0, out, 0, iv.length);
      System.arraycopy(ct, 0, out, iv.length, ct.length);
      return out;
    } catch (Exception e) {
      throw new IllegalStateException("encrypt failed", e);
    }
  }

  public byte[] decrypt(byte[] blob) {
    try {
      byte[] iv = new byte[GCM_IV_BYTES];
      System.arraycopy(blob, 0, iv, 0, GCM_IV_BYTES);
      byte[] ct = new byte[blob.length - GCM_IV_BYTES];
      System.arraycopy(blob, GCM_IV_BYTES, ct, 0, ct.length);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
      return cipher.doFinal(ct);
    } catch (Exception e) {
      throw new IllegalStateException("decrypt failed", e);
    }
  }
}
