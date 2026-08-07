package com.hirevo.security.jwt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hirevo.security.jwt")
public class JwtProperties {

  /** Issuer claim (iss) — usually the API base URL. */
  private String issuer = "https://api.hirevo.id";

  /** Access token lifetime — short, non-revocable. */
  private Duration accessTokenTtl = Duration.ofMinutes(15);

  /** Refresh token lifetime — long, revocable via Redis. */
  private Duration refreshTokenTtl = Duration.ofDays(7);

  /**
   * HMAC secret for HS256 signing (dev/local convenience).
   * <p>Production MUST use RS256 with keypair loaded from KMS — see {@link JwtService}.
   */
  private String hmacSecret =
      "change-me-locally-only-in-dev-use-rs256-with-kms-in-prod-32chars-minimum!!!";

  /** Algorithm: {@code HS256} (dev) or {@code RS256} (prod). */
  private String algorithm = "HS256";

  /** RSA private key PEM (RS256). Loaded from KMS/secrets manager in prod. */
  private String privateKeyPem;

  /** RSA public key PEM (RS256). Published via JWKS endpoint. */
  private String publicKeyPem;

  public String getIssuer() { return issuer; }
  public void setIssuer(String issuer) { this.issuer = issuer; }
  public Duration getAccessTokenTtl() { return accessTokenTtl; }
  public void setAccessTokenTtl(Duration accessTokenTtl) { this.accessTokenTtl = accessTokenTtl; }
  public Duration getRefreshTokenTtl() { return refreshTokenTtl; }
  public void setRefreshTokenTtl(Duration refreshTokenTtl) { this.refreshTokenTtl = refreshTokenTtl; }
  public String getHmacSecret() { return hmacSecret; }
  public void setHmacSecret(String hmacSecret) { this.hmacSecret = hmacSecret; }
  public String getAlgorithm() { return algorithm; }
  public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
  public String getPrivateKeyPem() { return privateKeyPem; }
  public void setPrivateKeyPem(String privateKeyPem) { this.privateKeyPem = privateKeyPem; }
  public String getPublicKeyPem() { return publicKeyPem; }
  public void setPublicKeyPem(String publicKeyPem) { this.publicKeyPem = publicKeyPem; }
}
