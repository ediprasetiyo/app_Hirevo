package com.hirevo.employee.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeSummary(
    UUID id,
    String employeeNo,
    String fullName,
    String status,
    LocalDate hireDate,
    String contractType,
    String personalEmail,
    String phone) {}
