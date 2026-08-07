package com.hirevo.iam.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record LoginRequest(
    @Email @NotBlank String email,
    @NotBlank @Size(min = 8, max = 200) String password,
    UUID deviceId,
    String deviceName,
    String deviceFingerprint) {}
