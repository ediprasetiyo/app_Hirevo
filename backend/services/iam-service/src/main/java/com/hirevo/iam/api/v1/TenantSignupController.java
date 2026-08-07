package com.hirevo.iam.api.v1;

import com.hirevo.iam.api.dto.TenantSignupRequest;
import com.hirevo.iam.api.dto.TenantSignupResponse;
import com.hirevo.iam.application.TenantSignupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tenants")
@Tag(name = "Tenants")
public class TenantSignupController {

  private final TenantSignupService signupService;

  public TenantSignupController(TenantSignupService signupService) {
    this.signupService = signupService;
  }

  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public TenantSignupResponse signup(@Valid @RequestBody TenantSignupRequest req) {
    return signupService.signup(req);
  }
}
