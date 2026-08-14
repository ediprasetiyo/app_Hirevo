package com.hirevo.employee.domain.repository;

import com.hirevo.employee.domain.model.Company;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
  Optional<Company> findFirstByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
