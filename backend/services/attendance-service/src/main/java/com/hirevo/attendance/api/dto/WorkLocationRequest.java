package com.hirevo.attendance.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WorkLocationRequest(
    @NotBlank String name,
    String address,
    @NotNull BigDecimal latitude,
    @NotNull BigDecimal longitude,
    Integer radiusMeters) {}
