package com.hirevo.employee.api.v1;

import com.hirevo.employee.api.dto.CreateEmployeeRequest;
import com.hirevo.employee.api.dto.EmployeeDetail;
import com.hirevo.employee.api.dto.EmployeePage;
import com.hirevo.employee.api.dto.ResignRequest;
import com.hirevo.employee.api.dto.UpdateEmployeeRequest;
import com.hirevo.employee.application.EmployeeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/employees")
@Tag(name = "Employees")
public class EmployeeController {

  private final EmployeeService service;

  public EmployeeController(EmployeeService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EmployeeDetail create(@Valid @RequestBody CreateEmployeeRequest req) {
    return service.create(req);
  }

  @GetMapping
  public EmployeePage list(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.list(status, search, page, Math.min(size, 100));
  }

  @GetMapping("/{id}")
  public EmployeeDetail get(@PathVariable UUID id) {
    return service.get(id);
  }

  @PatchMapping("/{id}")
  public EmployeeDetail update(@PathVariable UUID id, @RequestBody UpdateEmployeeRequest req) {
    return service.update(id, req);
  }

  @PostMapping("/{id}/resign")
  public EmployeeDetail resign(@PathVariable UUID id, @Valid @RequestBody ResignRequest req) {
    service.resign(id, req);
    return service.get(id);
  }
}
