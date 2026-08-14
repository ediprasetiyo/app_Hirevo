package com.hirevo.payroll.api.v1;

import com.hirevo.payroll.api.dto.SalaryComponentDtos.CreateSalaryComponentRequest;
import com.hirevo.payroll.api.dto.SalaryComponentDtos.SalaryComponentResponse;
import com.hirevo.payroll.api.dto.SalaryStructureDtos.CreateSalaryStructureRequest;
import com.hirevo.payroll.api.dto.SalaryStructureDtos.SalaryStructureResponse;
import com.hirevo.payroll.application.PayrollService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class SalaryComponentController {

  private final PayrollService service;

  public SalaryComponentController(PayrollService service) {
    this.service = service;
  }

  @GetMapping("/v1/salary-components")
  public List<SalaryComponentResponse> list() {
    return service.listComponents();
  }

  @PostMapping("/v1/salary-components")
  @ResponseStatus(HttpStatus.CREATED)
  public SalaryComponentResponse create(@Valid @RequestBody CreateSalaryComponentRequest req) {
    return service.createComponent(req);
  }

  @GetMapping("/v1/salary-structures")
  public List<SalaryStructureResponse> listStructures(@RequestParam UUID employeeId) {
    return service.listStructures(employeeId);
  }

  @PostMapping("/v1/salary-structures")
  @ResponseStatus(HttpStatus.CREATED)
  public SalaryStructureResponse assign(@Valid @RequestBody CreateSalaryStructureRequest req) {
    return service.assignStructure(req);
  }
}
