package com.hirevo.iam.domain.repository;

import com.hirevo.iam.domain.model.UserMfaMethod;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMfaMethodRepository extends JpaRepository<UserMfaMethod, UUID> {
  List<UserMfaMethod> findByUserIdAndRevokedAtIsNull(UUID userId);
}
