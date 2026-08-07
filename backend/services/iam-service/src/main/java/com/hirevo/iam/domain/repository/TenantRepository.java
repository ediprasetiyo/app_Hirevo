package com.hirevo.iam.domain.repository;

import com.hirevo.iam.domain.model.Tenant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
  Optional<Tenant> findBySubdomain(String subdomain);
  boolean existsBySubdomain(String subdomain);
}
