package com.hirevo.iam.domain.repository;

import com.hirevo.iam.domain.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);
  boolean existsByTenantIdAndEmail(UUID tenantId, String email);
}
