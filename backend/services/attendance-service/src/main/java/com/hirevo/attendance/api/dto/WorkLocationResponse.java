package com.hirevo.attendance.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WorkLocationResponse(
    UUID id, String name, String address,
    BigDecimal latitude, BigDecimal longitude, Integer radiusMeters, boolean active) {}
