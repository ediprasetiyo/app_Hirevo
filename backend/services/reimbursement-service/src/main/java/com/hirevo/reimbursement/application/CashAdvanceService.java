package com.hirevo.reimbursement.application;

import com.hirevo.audit.Audited;
import com.hirevo.core.exception.BusinessException;
import com.hirevo.core.exception.NotFoundException;
import com.hirevo.reimbursement.api.dto.CashAdvanceDtos.CashAdvanceResponse;
import com.hirevo.reimbursement.api.dto.CashAdvanceDtos.CreateCashAdvanceRequest;
import com.hirevo.reimbursement.api.dto.CashAdvanceDtos.SettleCashAdvanceRequest;
import com.hirevo.reimbursement.domain.model.CashAdvance;
import com.hirevo.reimbursement.domain.model.CashAdvanceSettlement;
import com.hirevo.reimbursement.domain.repository.CashAdvanceRepository;
import com.hirevo.reimbursement.domain.repository.CashAdvanceSettlementRepository;
import com.hirevo.tenant.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cash advance request -> approve -> disburse -> settle lifecycle. */
@Service
public class CashAdvanceService {

  private final CashAdvanceRepository advances;
  private final CashAdvanceSettlementRepository settlements;

  public CashAdvanceService(CashAdvanceRepository advances, CashAdvanceSettlementRepository settlements) {
    this.advances = advances;
    this.settlements = settlements;
  }

  @Transactional
  @Audited(module = "reimbursement", action = "cash_advance_request", entityType = "cash_advance",
           entityIdExpression = "#result.id()")
  public CashAdvanceResponse create(CreateCashAdvanceRequest req) {
    if (req.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessException("Jumlah kasbon harus lebih dari 0");
    }
    CashAdvance a = new CashAdvance();
    a.setTenantId(TenantContext.getRequired());
    a.setEmployeeId(req.employeeId());
    a.setPurpose(req.purpose());
    a.setAmount(req.amount());
    a.setNeededDate(req.neededDate());
    advances.save(a);
    return toResponse(a);
  }

  @Transactional
  @Audited(module = "reimbursement", action = "cash_advance_approve", entityType = "cash_advance", entityIdExpression = "#id")
  public CashAdvanceResponse approve(UUID id) {
    CashAdvance a = get(id);
    if (!"pending".equals(a.getStatus())) {
      throw new BusinessException("Hanya kasbon berstatus pending yang bisa disetujui");
    }
    a.setStatus("approved");
    advances.save(a);
    return toResponse(a);
  }

  @Transactional
  @Audited(module = "reimbursement", action = "cash_advance_reject", entityType = "cash_advance", entityIdExpression = "#id")
  public CashAdvanceResponse reject(UUID id) {
    CashAdvance a = get(id);
    if (!"pending".equals(a.getStatus())) {
      throw new BusinessException("Hanya kasbon berstatus pending yang bisa ditolak");
    }
    a.setStatus("rejected");
    advances.save(a);
    return toResponse(a);
  }

  @Transactional
  @Audited(module = "reimbursement", action = "cash_advance_disburse", entityType = "cash_advance", entityIdExpression = "#id")
  public CashAdvanceResponse disburse(UUID id) {
    CashAdvance a = get(id);
    if (!"approved".equals(a.getStatus())) {
      throw new BusinessException("Hanya kasbon berstatus approved yang bisa dicairkan");
    }
    a.setStatus("disbursed");
    a.setDisbursedAt(Instant.now());
    advances.save(a);
    return toResponse(a);
  }

  @Transactional
  @Audited(module = "reimbursement", action = "cash_advance_settle", entityType = "cash_advance", entityIdExpression = "#id")
  public CashAdvanceResponse settle(UUID id, SettleCashAdvanceRequest req) {
    CashAdvance a = get(id);
    if (!"disbursed".equals(a.getStatus())) {
      throw new BusinessException("Hanya kasbon berstatus disbursed yang bisa diselesaikan");
    }
    BigDecimal refund = req.settledAmount().compareTo(a.getAmount()) < 0
        ? a.getAmount().subtract(req.settledAmount()) : BigDecimal.ZERO;
    BigDecimal shortfall = req.settledAmount().compareTo(a.getAmount()) > 0
        ? req.settledAmount().subtract(a.getAmount()) : BigDecimal.ZERO;

    CashAdvanceSettlement s = new CashAdvanceSettlement();
    s.setTenantId(a.getTenantId());
    s.setCashAdvanceId(a.getId());
    s.setSettledAmount(req.settledAmount());
    s.setRefundAmount(refund);
    s.setShortfallAmount(shortfall);
    settlements.save(s);

    a.setStatus("settled");
    a.setSettledAt(Instant.now());
    advances.save(a);
    return toResponse(a);
  }

  @Transactional(readOnly = true)
  public List<CashAdvanceResponse> list(UUID employeeId) {
    List<CashAdvance> list = employeeId == null
        ? advances.findAllByOrderByCreatedAtDesc() : advances.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    return list.stream().map(this::toResponse).toList();
  }

  private CashAdvance get(UUID id) {
    return advances.findById(id).orElseThrow(() -> new NotFoundException("CashAdvance", id));
  }

  private CashAdvanceResponse toResponse(CashAdvance a) {
    return new CashAdvanceResponse(a.getId(), a.getEmployeeId(), a.getRequestNo(), a.getPurpose(),
        a.getAmount(), a.getNeededDate(), a.getStatus(), a.getDisbursedAt(), a.getSettledAt(), a.getCreatedAt());
  }
}
