package com.hirevo.employee.api.dto;

import java.util.List;

/**
 * Offset-based pagination (page/size/totalElements) — simpler to implement
 * correctly than cursor-based pagination for the MVP. Deviates from the
 * cursor-based convention in the architecture docs; revisit if list sizes
 * grow large enough for offset pagination's performance cost to matter.
 */
public record EmployeePage(List<EmployeeSummary> data, PageInfo pagination) {
  public record PageInfo(int page, int size, long totalElements, int totalPages) {}
}
