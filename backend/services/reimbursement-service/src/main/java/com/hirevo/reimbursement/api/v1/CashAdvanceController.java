package com.hirevo.reimbursement.api.v1;

import com.hirevo.reimbursement.api.dto.CashAdvanceDtos.CashAdvanceResponse;
import com.hirevo.reimbursement.api.dto.CashAdvanceDtos.CreateCashAdvanceRequest;
import com.hirevo.reimbursement.api.dto.CashAdvanceDtos.SettleCashAdvanceRequest;
import com.hirevo.reimbursement.application.CashAdvanceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cash-advances")
public class CashAdvanceController {

  private final CashAdvanceService service;

  public CashAdvanceController(CashAdvanceService service) {
    this.service = service;
  }

  @GetMapping
  public List<CashAdvanceResponse> list(@RequestParam(required = false) UUID employeeId) {
    return service.list(employeeId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CashAdvanceResponse create(@Valid @RequestBody CreateCashAdvanceRequest req) {
    return service.create(req);
  }

  @PostMapping("/{id}/approve")
  public CashAdvanceResponse approve(@PathVariable UUID id) {
    return service.approve(id);
  }

  @PostMapping("/{id}/reject")
  public CashAdvanceResponse reject(@PathVariable UUID id) {
    return service.reject(id);
  }

  @PostMapping("/{id}/disburse")
  public CashAdvanceResponse disburse(@PathVariable UUID id) {
    return service.disburse(id);
  }

  @PostMapping("/{id}/settle")
  public CashAdvanceResponse settle(@PathVariable UUID id, @Valid @RequestBody SettleCashAdvanceRequest req) {
    return service.settle(id, req);
  }
}
