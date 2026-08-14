package com.hirevo.reimbursement.api.v1;

import com.hirevo.reimbursement.api.dto.ReimbursementDtos.CategoryResponse;
import com.hirevo.reimbursement.api.dto.ReimbursementDtos.CreateRequest;
import com.hirevo.reimbursement.api.dto.ReimbursementDtos.RequestResponse;
import com.hirevo.reimbursement.application.ReimbursementService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReimbursementController {

  private final ReimbursementService service;

  public ReimbursementController(ReimbursementService service) {
    this.service = service;
  }

  @GetMapping("/v1/reimbursement-categories")
  public List<CategoryResponse> listCategories() {
    return service.listCategories();
  }

  @PostMapping("/v1/reimbursement-requests")
  @ResponseStatus(HttpStatus.CREATED)
  public RequestResponse submit(@Valid @RequestBody CreateRequest req) {
    return service.submit(req);
  }

  @GetMapping("/v1/reimbursement-requests")
  public List<RequestResponse> list(@RequestParam(required = false) UUID employeeId) {
    return service.listRequests(employeeId);
  }

  @PostMapping("/v1/reimbursement-requests/{id}/approve")
  public RequestResponse approve(@PathVariable UUID id) {
    return service.approve(id);
  }

  @PostMapping("/v1/reimbursement-requests/{id}/reject")
  public RequestResponse reject(@PathVariable UUID id) {
    return service.reject(id);
  }

  @PostMapping("/v1/reimbursement-requests/{id}/mark-paid")
  public RequestResponse markPaid(@PathVariable UUID id) {
    return service.markPaid(id);
  }
}
