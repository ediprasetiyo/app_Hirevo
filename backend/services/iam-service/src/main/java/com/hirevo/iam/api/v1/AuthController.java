package com.hirevo.iam.api.v1;

import com.hirevo.iam.api.dto.LoginRequest;
import com.hirevo.iam.api.dto.LoginResponse;
import com.hirevo.iam.api.dto.MfaVerifyRequest;
import com.hirevo.iam.api.dto.RefreshRequest;
import com.hirevo.iam.application.AuthService;
import com.hirevo.iam.application.MfaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

  private final AuthService authService;
  private final MfaService mfaService;

  public AuthController(AuthService authService, MfaService mfaService) {
    this.authService = authService;
    this.mfaService = mfaService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest req,
      @RequestHeader(value = "X-Tenant-Subdomain") String subdomain) {
    LoginResponse resp = authService.login(req, subdomain);
    if (Boolean.TRUE.equals(resp.mfaRequired())) {
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(resp);
    }
    return ResponseEntity.ok(resp);
  }

  @PostMapping("/refresh")
  public LoginResponse refresh(@Valid @RequestBody RefreshRequest req) {
    return authService.refresh(req);
  }

  @PostMapping("/mfa/verify")
  public LoginResponse verifyMfa(@Valid @RequestBody MfaVerifyRequest req) {
    return mfaService.verifyLoginChallenge(req);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@Valid @RequestBody RefreshRequest req) {
    authService.logout(req.refreshToken());
  }
}
