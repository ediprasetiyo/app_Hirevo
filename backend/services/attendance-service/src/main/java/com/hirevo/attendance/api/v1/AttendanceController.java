package com.hirevo.attendance.api.v1;

import com.hirevo.attendance.api.dto.AttendanceLogResponse;
import com.hirevo.attendance.api.dto.ClockRequest;
import com.hirevo.attendance.application.AttendanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/attendance")
@Tag(name = "Attendance")
public class AttendanceController {

  private final AttendanceService service;

  public AttendanceController(AttendanceService service) {
    this.service = service;
  }

  @PostMapping("/clock-in")
  public AttendanceLogResponse clockIn(@Valid @RequestBody ClockRequest req) {
    return service.clockIn(req);
  }

  @PostMapping("/clock-out")
  public AttendanceLogResponse clockOut(@Valid @RequestBody ClockRequest req) {
    return service.clockOut(req);
  }

  @GetMapping("/logs")
  public List<AttendanceLogResponse> logs(
      @RequestParam(required = false) UUID employeeId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    LocalDate effectiveTo = to == null ? LocalDate.now() : to;
    LocalDate effectiveFrom = from == null ? effectiveTo.minusDays(30) : from;
    return employeeId == null
        ? service.listAll(effectiveFrom, effectiveTo)
        : service.listForEmployee(employeeId, effectiveFrom, effectiveTo);
  }
}
