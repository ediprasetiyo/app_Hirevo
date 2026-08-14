package com.hirevo.payroll.api.v1;

import com.hirevo.payroll.api.dto.PayrollRunDtos.CreatePayrollRunRequest;
import com.hirevo.payroll.api.dto.PayrollRunDtos.PayrollRunResponse;
import com.hirevo.payroll.api.dto.PayslipDtos.PayslipResponse;
import com.hirevo.payroll.application.PayrollService;
import com.hirevo.security.jwt.HirevoPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payroll-runs")
public class PayrollRunController {

  private final PayrollService service;

  public PayrollRunController(PayrollService service) {
    this.service = service;
  }

  @GetMapping
  public List<PayrollRunResponse> list() {
    return service.listRuns();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PayrollRunResponse create(@Valid @RequestBody CreatePayrollRunRequest req) {
    return service.createRun(req.payrollPeriodId());
  }

  @PostMapping("/{id}/calculate")
  public PayrollRunResponse calculate(@PathVariable UUID id) {
    return service.calculateRun(id);
  }

  @PostMapping("/{id}/approve")
  public PayrollRunResponse approve(@PathVariable UUID id, @AuthenticationPrincipal HirevoPrincipal principal) {
    return service.approveRun(id, principal.userId());
  }

  @PostMapping("/{id}/mark-paid")
  public PayrollRunResponse markPaid(@PathVariable UUID id) {
    return service.markPaid(id);
  }

  @GetMapping("/{id}/payslips")
  public List<PayslipResponse> payslips(@PathVariable UUID id) {
    return service.listPayslips(id);
  }
}
