package com.hirevo.reimbursement.domain.repository;

import com.hirevo.reimbursement.domain.model.CashAdvanceSettlement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashAdvanceSettlementRepository extends JpaRepository<CashAdvanceSettlement, UUID> {
}
