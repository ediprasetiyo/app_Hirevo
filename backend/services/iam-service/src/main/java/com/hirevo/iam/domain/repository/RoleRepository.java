package com.hirevo.iam.domain.repository;

import com.hirevo.iam.domain.model.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {
  Optional<Role> findByTenantIdAndName(UUID tenantId, String name);
  Optional<Role> findBySystemTrueAndName(String name);
}
