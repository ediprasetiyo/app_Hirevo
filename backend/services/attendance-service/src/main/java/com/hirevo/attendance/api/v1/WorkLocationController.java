package com.hirevo.attendance.api.v1;

import com.hirevo.attendance.api.dto.WorkLocationRequest;
import com.hirevo.attendance.api.dto.WorkLocationResponse;
import com.hirevo.attendance.application.WorkLocationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/work-locations")
@Tag(name = "Work Locations")
public class WorkLocationController {

  private final WorkLocationService service;

  public WorkLocationController(WorkLocationService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public WorkLocationResponse create(@Valid @RequestBody WorkLocationRequest req) {
    return service.create(req);
  }

  @GetMapping
  public List<WorkLocationResponse> list() {
    return service.list();
  }
}
