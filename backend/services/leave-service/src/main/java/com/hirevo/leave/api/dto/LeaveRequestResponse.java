package com.hirevo.leave.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveRequestResponse(
    UUID id, UUID employeeId, UUID leaveTypeId, String leaveTypeName,
    LocalDate startDate, LocalDate endDate, BigDecimal totalDays,
    String reason, String status) {}
