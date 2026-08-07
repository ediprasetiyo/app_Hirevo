package com.hirevo.iam.api.v1;

import com.hirevo.iam.application.MfaService;
import com.hirevo.security.jwt.HirevoPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/mfa")
@Tag(name = "MFA")
public class MfaController {

  private final MfaService mfaService;

  public MfaController(MfaService mfaService) {
    this.mfaService = mfaService;
  }

  @PostMapping("/enroll/totp/begin")
  public Map<String, Object> beginTotp(@AuthenticationPrincipal HirevoPrincipal p,
                                        @RequestParam String email) {
    return mfaService.beginTotpEnrollment(p.userId(), email);
  }

  public record ConfirmTotpRequest(UUID methodId, @NotBlank String code) {}

  @PostMapping("/enroll/totp/confirm")
  public Map<String, Object> confirmTotp(@AuthenticationPrincipal HirevoPrincipal p,
                                          @RequestBody ConfirmTotpRequest req) {
    mfaService.confirmTotpEnrollment(p.userId(), req.methodId(), req.code());
    return Map.of("enrolled", true);
  }
}
