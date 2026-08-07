package com.hirevo.iam.api.dto;

import java.util.UUID;

public record TenantSignupResponse(
    UUID tenantId,
    String subdomain,
    UUID adminUserId,
    String tenantUrl) {}
