package com.hirevo.leave.api.v1;

import com.hirevo.leave.api.dto.CreateLeaveRequest;
import com.hirevo.leave.api.dto.LeaveBalanceResponse;
import com.hirevo.leave.api.dto.LeaveRequestResponse;
import com.hirevo.leave.api.dto.LeaveTypeResponse;
import com.hirevo.leave.application.LeaveService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Leave")
public class LeaveController {

  private final LeaveService service;

  public LeaveController(LeaveService service) {
    this.service = service;
  }

  @GetMapping("/v1/leave-types")
  public List<LeaveTypeResponse> types() {
    return service.listTypes();
  }

  @GetMapping("/v1/leave-balances")
  public List<LeaveBalanceResponse> balances(
      @RequestParam UUID employeeId,
      @RequestParam(required = false) Integer year) {
    return service.listBalances(employeeId, year == null ? LocalDate.now().getYear() : year);
  }

  @PostMapping("/v1/leave-requests")
  @ResponseStatus(HttpStatus.CREATED)
  public LeaveRequestResponse submit(@Valid @RequestBody CreateLeaveRequest req) {
    return service.submit(req);
  }

  @GetMapping("/v1/leave-requests")
  public List<LeaveRequestResponse> list(@RequestParam(required = false) UUID employeeId) {
    return service.listRequests(employeeId);
  }

  @PostMapping("/v1/leave-requests/{id}/approve")
  public LeaveRequestResponse approve(@PathVariable UUID id) {
    return service.approve(id);
  }

  @PostMapping("/v1/leave-requests/{id}/reject")
  public LeaveRequestResponse reject(@PathVariable UUID id) {
    return service.reject(id);
  }
}
