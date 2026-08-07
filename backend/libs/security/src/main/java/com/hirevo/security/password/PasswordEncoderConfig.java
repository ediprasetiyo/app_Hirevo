package com.hirevo.security.password;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Argon2id password encoder — OWASP 2024 recommendation.
 * Parameters chosen for ~500ms verification on a 4-core cloud VM.
 */
@Configuration
public class PasswordEncoderConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    // saltLength=16, hashLength=32, parallelism=4, memory=65536 KiB (64MiB), iterations=3
    return new Argon2PasswordEncoder(16, 32, 4, 65536, 3);
  }
}
