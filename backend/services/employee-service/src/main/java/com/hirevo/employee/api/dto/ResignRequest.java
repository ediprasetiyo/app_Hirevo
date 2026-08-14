package com.hirevo.employee.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ResignRequest(@NotNull LocalDate resignDate, String reason) {}
