package com.hirevo.employee.api.dto;

public record UpdateEmployeeRequest(
    String fullName,
    String phone,
    String personalEmail,
    String address) {}
