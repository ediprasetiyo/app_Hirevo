package com.hirevo.employee.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeDetail(
    UUID id,
    String employeeNo,
    String fullName,
    String nikMasked,
    String npwpMasked,
    LocalDate dateOfBirth,
    String gender,
    String maritalStatus,
    String personalEmail,
    String phone,
    String address,
    LocalDate hireDate,
    LocalDate resignDate,
    String status,
    ContractInfo activeContract) {

  public record ContractInfo(
      UUID id,
      String contractType,
      LocalDate startDate,
      LocalDate endDate,
      BigDecimal baseSalary,
      String workArrangement,
      String status) {}
}
