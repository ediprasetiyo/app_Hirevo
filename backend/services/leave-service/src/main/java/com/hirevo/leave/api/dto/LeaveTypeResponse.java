package com.hirevo.leave.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LeaveTypeResponse(
    UUID id, String code, String name, boolean paid, BigDecimal defaultDaysPerYear,
    boolean requireAttachment) {}
