package com.hirevo.reimbursement.domain.repository;

import com.hirevo.reimbursement.domain.model.ReimbursementItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReimbursementItemRepository extends JpaRepository<ReimbursementItem, UUID> {
  List<ReimbursementItem> findByReimbursementRequestId(UUID requestId);
}
