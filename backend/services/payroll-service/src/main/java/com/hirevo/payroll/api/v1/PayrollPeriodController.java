package com.hirevo.payroll.api.v1;

import com.hirevo.payroll.api.dto.PayrollPeriodDtos.CreatePayrollPeriodRequest;
import com.hirevo.payroll.api.dto.PayrollPeriodDtos.PayrollPeriodResponse;
import com.hirevo.payroll.application.PayrollService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payroll-periods")
public class PayrollPeriodController {

  private final PayrollService service;

  public PayrollPeriodController(PayrollService service) {
    this.service = service;
  }

  @GetMapping
  public List<PayrollPeriodResponse> list() {
    return service.listPeriods();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PayrollPeriodResponse create(@Valid @RequestBody CreatePayrollPeriodRequest req) {
    return service.createPeriod(req);
  }
}
