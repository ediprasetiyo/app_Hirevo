package com.hirevo.payroll.api.v1;

import com.hirevo.payroll.api.dto.PayslipDtos.PayslipResponse;
import com.hirevo.payroll.application.PayrollService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payslips")
public class PayslipController {

  private final PayrollService service;

  public PayslipController(PayrollService service) {
    this.service = service;
  }

  @GetMapping
  public List<PayslipResponse> list(@RequestParam UUID employeeId) {
    return service.listPayslipsForEmployee(employeeId);
  }

  @GetMapping("/{id}")
  public PayslipResponse get(@PathVariable UUID id) {
    return service.getPayslip(id);
  }
}
