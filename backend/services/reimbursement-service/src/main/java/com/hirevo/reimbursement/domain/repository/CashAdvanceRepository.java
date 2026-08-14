package com.hirevo.reimbursement.domain.repository;

import com.hirevo.reimbursement.domain.model.CashAdvance;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashAdvanceRepository extends JpaRepository<CashAdvance, UUID> {
  List<CashAdvance> findAllByOrderByCreatedAtDesc();
  List<CashAdvance> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);
}
