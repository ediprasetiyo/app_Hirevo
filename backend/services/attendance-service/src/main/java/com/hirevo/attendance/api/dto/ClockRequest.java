package com.hirevo.attendance.api.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ClockRequest(
    UUID employeeId,
    @NotNull BigDecimal latitude,
    @NotNull BigDecimal longitude,
    BigDecimal accuracyMeters,
    boolean isMockLocation,
    String source) {}
