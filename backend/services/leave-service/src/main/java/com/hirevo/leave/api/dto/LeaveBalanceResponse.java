package com.hirevo.leave.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LeaveBalanceResponse(
    UUID leaveTypeId, String leaveTypeCode, String leaveTypeName,
    Integer year, BigDecimal initialBalance, BigDecimal carryOver,
    BigDecimal used, BigDecimal pending, BigDecimal remaining) {}
