package com.hirevo.reimbursement.domain.repository;

import com.hirevo.reimbursement.domain.model.ReimbursementCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReimbursementCategoryRepository extends JpaRepository<ReimbursementCategory, UUID> {
  boolean existsByTenantId(UUID tenantId);
  List<ReimbursementCategory> findByTenantIdAndActiveTrue(UUID tenantId);
}
