package com.hirevo.employee.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateEmployeeRequest(
    @NotBlank String employeeNo,
    @NotBlank String fullName,
    @Pattern(regexp = "^$|^[0-9]{16}$", message = "NIK must be 16 digits") String nik,
    String npwp,
    LocalDate dateOfBirth,
    String gender,
    String maritalStatus,
    String personalEmail,
    String phone,
    String address,
    @NotNull LocalDate hireDate,
    @NotNull ContractInput contract) {

  public record ContractInput(
      @NotBlank String contractType,
      @NotNull LocalDate startDate,
      LocalDate endDate,
      @NotNull BigDecimal baseSalary,
      String workArrangement) {}
}
