package com.hirevo.iam.api.v1;

import com.hirevo.security.jwt.HirevoPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/me")
@Tag(name = "Me")
public class MeController {

  @GetMapping
  public Map<String, Object> me(@AuthenticationPrincipal HirevoPrincipal principal) {
    return Map.of(
        "user_id", principal.userId(),
        "tenant_id", principal.tenantId(),
        "roles", principal.roles(),
        "permissions", principal.permissions());
  }
}
