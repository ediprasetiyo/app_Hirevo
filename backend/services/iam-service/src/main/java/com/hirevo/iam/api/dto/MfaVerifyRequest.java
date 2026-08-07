package com.hirevo.iam.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record MfaVerifyRequest(
    @NotBlank String challengeId,
    UUID methodId,
    @NotBlank String code) {}
