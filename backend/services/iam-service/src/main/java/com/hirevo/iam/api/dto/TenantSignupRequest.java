package com.hirevo.iam.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TenantSignupRequest(
    @NotBlank @Size(min = 2, max = 200) String companyName,
    @NotBlank @Size(min = 3, max = 63)
    @Pattern(regexp = "^[a-z0-9](?:[a-z0-9-]{1,61}[a-z0-9])?$",
             message = "Subdomain: lowercase letters, digits, hyphens (not at start/end).")
    String subdomain,
    @Email @NotBlank String adminEmail,
    @NotBlank @Size(min = 8, max = 200) String adminPassword,
    @NotBlank @Size(min = 2, max = 200) String adminFullName) {}
