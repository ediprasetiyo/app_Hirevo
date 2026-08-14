package com.hirevo.leave.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateLeaveRequest(
    @NotNull UUID employeeId,
    @NotNull UUID leaveTypeId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    String reason) {}
