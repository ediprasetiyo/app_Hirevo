package com.hirevo.attendance.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceLogResponse(
    UUID id,
    UUID employeeId,
    LocalDate workDate,
    Instant clockInAt,
    Instant clockOutAt,
    String status,
    Integer lateMinutes,
    Integer workedMinutes,
    Integer fraudScore,
    boolean anomaly,
    String anomalyReason) {}
