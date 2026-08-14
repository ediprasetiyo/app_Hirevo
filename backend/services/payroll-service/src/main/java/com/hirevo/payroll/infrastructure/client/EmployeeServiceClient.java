package com.hirevo.payroll.infrastructure.client;

import com.hirevo.tenant.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Reads employee + active-contract data from employee-service over HTTP.
 *
 * <p>This is the first inter-service call in the codebase — no gateway sits
 * in front of these services yet in local dev, so payroll-service calls
 * employee-service directly on its own port (same pattern the frontend
 * already uses). The incoming request's {@code Authorization} bearer token
 * is forwarded as-is (all services trust the same JWT HMAC secret, see
 * hirevo-security's JwtService), plus {@code X-Tenant-ID} from the current
 * {@link TenantContext} so employee-service's own tenant/RLS resolution
 * lines up with the caller's session rather than falling back to whatever
 * the forwarded JWT's tenant claim says.
 */
@Component
public class EmployeeServiceClient {

  private final RestClient restClient;

  public EmployeeServiceClient(@Value("${hirevo.services.employee-url}") String baseUrl) {
    this.restClient = RestClient.builder().baseUrl(baseUrl).build();
  }

  public record ContractInfo(BigDecimal baseSalary, String contractType, LocalDate startDate) {}

  public record EmployeeInfo(
      UUID id, String employeeNo, String fullName, String status,
      String maritalStatus, ContractInfo activeContract) {}

  public record PageInfo(int page, int size, long totalElements, int totalPages) {}
  public record SummaryRow(UUID id, String employeeNo, String fullName, String status) {}
  public record PageResponse(List<SummaryRow> data, PageInfo pagination) {}

  public List<SummaryRow> listActiveEmployees() {
    PageResponse page = restClient.get()
        .uri(uri -> uri.path("/employees")
            .queryParam("status", "active")
            .queryParam("size", 100)
            .build())
        .header("Authorization", currentAuthHeader())
        .header("X-Tenant-ID", TenantContext.getRequired().toString())
        .retrieve()
        .body(PageResponse.class);
    return page == null ? List.of() : page.data();
  }

  public EmployeeInfo getEmployee(UUID id) {
    return restClient.get()
        .uri("/employees/{id}", id)
        .header("Authorization", currentAuthHeader())
        .header("X-Tenant-ID", TenantContext.getRequired().toString())
        .retrieve()
        .body(EmployeeInfo.class);
  }

  private String currentAuthHeader() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      throw new IllegalStateException("No inbound request context to forward auth from");
    }
    return attrs.getRequest().getHeader("Authorization");
  }
}
