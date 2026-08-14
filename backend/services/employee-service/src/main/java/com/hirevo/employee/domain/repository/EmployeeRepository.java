package com.hirevo.employee.domain.repository;

import com.hirevo.employee.domain.model.Employee;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

  Optional<Employee> findByIdAndDeletedAtIsNull(UUID id);

  boolean existsByTenantIdAndEmployeeNo(UUID tenantId, String employeeNo);

  // CAST(:search AS string) is required — without it, Postgres cannot infer a
  // type for the bind parameter when the Java value is null (no search term
  // typed), and its type-inference fallback picked BYTEA (matching the
  // entity's nik_encrypted/npwp_encrypted columns elsewhere in this query),
  // producing "function lower(bytea) does not exist" only on the no-filter
  // path — the exact request shape a fresh empty employee list hits first.
  @Query("""
      SELECT e FROM Employee e
      WHERE e.deletedAt IS NULL
        AND (:status IS NULL OR e.status = :status)
        AND (:search IS NULL
             OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
             OR LOWER(e.employeeNo) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
      ORDER BY e.createdAt DESC
      """)
  Page<Employee> search(@Param("status") String status, @Param("search") String search, Pageable pageable);

  long countByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, String status);
}
