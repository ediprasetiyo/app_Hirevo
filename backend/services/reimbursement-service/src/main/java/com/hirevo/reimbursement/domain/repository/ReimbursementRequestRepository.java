package com.hirevo.reimbursement.domain.repository;

import com.hirevo.reimbursement.domain.model.ReimbursementRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReimbursementRequestRepository extends JpaRepository<ReimbursementRequest, UUID> {
  List<ReimbursementRequest> findAllByOrderByCreatedAtDesc();
  List<ReimbursementRequest> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);
}
