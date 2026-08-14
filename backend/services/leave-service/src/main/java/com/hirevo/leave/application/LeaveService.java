package com.hirevo.leave.application;

import com.hirevo.audit.Audited;
import com.hirevo.core.exception.BusinessException;
import com.hirevo.core.exception.NotFoundException;
import com.hirevo.leave.api.dto.CreateLeaveRequest;
import com.hirevo.leave.api.dto.LeaveBalanceResponse;
import com.hirevo.leave.api.dto.LeaveRequestResponse;
import com.hirevo.leave.api.dto.LeaveTypeResponse;
import com.hirevo.leave.domain.model.LeaveBalance;
import com.hirevo.leave.domain.model.LeaveRequest;
import com.hirevo.leave.domain.model.LeaveType;
import com.hirevo.leave.domain.repository.LeaveBalanceRepository;
import com.hirevo.leave.domain.repository.LeaveRequestRepository;
import com.hirevo.leave.domain.repository.LeaveTypeRepository;
import com.hirevo.tenant.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Leave request + balance management.
 *
 * <p><b>Scope simplifications (MVP, documented not hidden):</b>
 * <ul>
 *   <li>Standard leave types (per UU 13/2003) are seeded globally with
 *       tenant_id=NULL in Liquibase, but the generic RLS policy applied to
 *       every table in this schema makes NULL-tenant rows invisible once a
 *       real tenant session is active (COALESCE sentinel only matches when
 *       no tenant context is set at all). Rather than special-case RLS for
 *       this one table, leave types are bootstrapped per-tenant on first
 *       access — same pattern as employee-service's default Company.</li>
 *   <li>totalDays counts calendar days inclusive (endDate - startDate + 1) —
 *       does not exclude weekends or public holidays. A real implementation
 *       would consult the holidays table from employee-service.</li>
 *   <li>Approval is single-step (any authenticated caller can approve/reject)
 *       — the full N-level workflow-service integration doesn't exist yet.</li>
 * </ul>
 */
@Service
public class LeaveService {

  private final LeaveTypeRepository types;
  private final LeaveBalanceRepository balances;
  private final LeaveRequestRepository requests;

  public LeaveService(LeaveTypeRepository types, LeaveBalanceRepository balances,
                      LeaveRequestRepository requests) {
    this.types = types;
    this.balances = balances;
    this.requests = requests;
  }

  private static final List<StandardType> STANDARD_TYPES = List.of(
      new StandardType("annual", "Cuti Tahunan", true, new BigDecimal("12"), new BigDecimal("6")),
      new StandardType("sick", "Cuti Sakit", true, new BigDecimal("365"), BigDecimal.ZERO),
      new StandardType("maternity", "Cuti Melahirkan", true, new BigDecimal("90"), BigDecimal.ZERO),
      new StandardType("paternity", "Cuti Suami Menemani Istri", true, new BigDecimal("2"), BigDecimal.ZERO),
      new StandardType("marriage", "Cuti Menikah", true, new BigDecimal("3"), BigDecimal.ZERO),
      new StandardType("bereavement", "Cuti Duka (Keluarga Inti)", true, new BigDecimal("2"), BigDecimal.ZERO),
      new StandardType("religious", "Cuti Ibadah (Haji/Umrah)", true, new BigDecimal("40"), BigDecimal.ZERO),
      new StandardType("menstruation", "Cuti Haid (Opsional)", true, new BigDecimal("2"), BigDecimal.ZERO),
      new StandardType("unpaid", "Cuti Tanpa Bayaran", false, BigDecimal.ZERO, BigDecimal.ZERO),
      new StandardType("permit", "Izin", true, BigDecimal.ZERO, BigDecimal.ZERO),
      new StandardType("wfh", "Work From Home", true, BigDecimal.ZERO, BigDecimal.ZERO)
  );

  private record StandardType(String code, String name, boolean paid, BigDecimal days, BigDecimal carryMax) {}

  @Transactional
  public List<LeaveTypeResponse> listTypes() {
    UUID tenantId = TenantContext.getRequired();
    if (!types.existsByTenantId(tenantId)) {
      bootstrapTypes(tenantId);
    }
    return types.findByTenantIdAndActiveTrue(tenantId).stream()
        .map(t -> new LeaveTypeResponse(t.getId(), t.getCode(), t.getName(), t.isPaid(),
            t.getDefaultDaysPerYear(), t.isRequireAttachment()))
        .toList();
  }

  private void bootstrapTypes(UUID tenantId) {
    for (StandardType st : STANDARD_TYPES) {
      LeaveType t = new LeaveType();
      t.setTenantId(tenantId);
      t.setCode(st.code());
      t.setName(st.name());
      t.setPaid(st.paid());
      t.setDefaultDaysPerYear(st.days());
      t.setCarryOverMaxDays(st.carryMax());
      t.setSystem(true);
      types.save(t);
    }
  }

  @Transactional
  public List<LeaveBalanceResponse> listBalances(UUID employeeId, int year) {
    listTypes(); // ensures bootstrap ran
    UUID tenantId = TenantContext.getRequired();
    List<LeaveType> activeTypes = types.findByTenantIdAndActiveTrue(tenantId);
    return activeTypes.stream()
        .map(t -> toBalanceResponse(t, getOrCreateBalance(employeeId, t, year)))
        .toList();
  }

  private LeaveBalance getOrCreateBalance(UUID employeeId, LeaveType type, int year) {
    return balances.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, type.getId(), year)
        .orElseGet(() -> {
          LeaveBalance b = new LeaveBalance();
          b.setTenantId(TenantContext.getRequired());
          b.setEmployeeId(employeeId);
          b.setLeaveTypeId(type.getId());
          b.setYear(year);
          b.setInitialBalance(type.getDefaultDaysPerYear() == null ? BigDecimal.ZERO : type.getDefaultDaysPerYear());
          return balances.save(b);
        });
  }

  @Transactional
  @Audited(module = "leave", action = "submit", entityType = "leave_request",
           entityIdExpression = "#result.id()")
  public LeaveRequestResponse submit(CreateLeaveRequest req) {
    if (req.endDate().isBefore(req.startDate())) {
      throw new BusinessException("Tanggal akhir tidak boleh sebelum tanggal mulai");
    }
    LeaveType type = types.findById(req.leaveTypeId())
        .orElseThrow(() -> new NotFoundException("LeaveType", req.leaveTypeId()));

    BigDecimal totalDays = BigDecimal.valueOf(
        ChronoUnit.DAYS.between(req.startDate(), req.endDate()) + 1);

    int year = req.startDate().getYear();
    LeaveBalance balance = getOrCreateBalance(req.employeeId(), type, year);
    BigDecimal remaining = balance.getInitialBalance().add(balance.getCarryOver())
        .subtract(balance.getUsed()).subtract(balance.getPending());
    if (type.isPaid() && remaining.compareTo(totalDays) < 0) {
      throw new BusinessException(
          "Saldo cuti tidak cukup — sisa " + remaining + " hari, mengajukan " + totalDays + " hari");
    }

    LeaveRequest lr = new LeaveRequest();
    lr.setTenantId(TenantContext.getRequired());
    lr.setEmployeeId(req.employeeId());
    lr.setLeaveTypeId(req.leaveTypeId());
    lr.setStartDate(req.startDate());
    lr.setEndDate(req.endDate());
    lr.setTotalDays(totalDays);
    lr.setReason(req.reason());
    lr.setStatus("pending");
    requests.save(lr);

    balance.setPending(balance.getPending().add(totalDays));
    balances.save(balance);

    return toRequestResponse(lr, type);
  }

  @Transactional
  @Audited(module = "leave", action = "approve", entityType = "leave_request", entityIdExpression = "#id")
  public LeaveRequestResponse approve(UUID id) {
    LeaveRequest lr = requests.findById(id).orElseThrow(() -> new NotFoundException("LeaveRequest", id));
    if (!"pending".equals(lr.getStatus())) {
      throw new BusinessException("Hanya pengajuan berstatus pending yang bisa disetujui");
    }
    LeaveType type = types.findById(lr.getLeaveTypeId())
        .orElseThrow(() -> new NotFoundException("LeaveType", lr.getLeaveTypeId()));
    LeaveBalance balance = getOrCreateBalance(lr.getEmployeeId(), type, lr.getStartDate().getYear());
    balance.setPending(balance.getPending().subtract(lr.getTotalDays()).max(BigDecimal.ZERO));
    balance.setUsed(balance.getUsed().add(lr.getTotalDays()));
    balances.save(balance);

    lr.setStatus("approved");
    lr.setApprovedAt(Instant.now());
    lr.setUpdatedAt(Instant.now());
    requests.save(lr);
    return toRequestResponse(lr, type);
  }

  @Transactional
  @Audited(module = "leave", action = "reject", entityType = "leave_request", entityIdExpression = "#id")
  public LeaveRequestResponse reject(UUID id) {
    LeaveRequest lr = requests.findById(id).orElseThrow(() -> new NotFoundException("LeaveRequest", id));
    if (!"pending".equals(lr.getStatus())) {
      throw new BusinessException("Hanya pengajuan berstatus pending yang bisa ditolak");
    }
    LeaveType type = types.findById(lr.getLeaveTypeId())
        .orElseThrow(() -> new NotFoundException("LeaveType", lr.getLeaveTypeId()));
    LeaveBalance balance = getOrCreateBalance(lr.getEmployeeId(), type, lr.getStartDate().getYear());
    balance.setPending(balance.getPending().subtract(lr.getTotalDays()).max(BigDecimal.ZERO));
    balances.save(balance);

    lr.setStatus("rejected");
    lr.setUpdatedAt(Instant.now());
    requests.save(lr);
    return toRequestResponse(lr, type);
  }

  @Transactional(readOnly = true)
  public List<LeaveRequestResponse> listRequests(UUID employeeId) {
    List<LeaveRequest> list = employeeId == null
        ? requests.findAllByOrderByCreatedAtDesc()
        : requests.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    return list.stream().map(lr -> {
      LeaveType type = types.findById(lr.getLeaveTypeId()).orElse(null);
      return toRequestResponse(lr, type);
    }).toList();
  }

  private LeaveBalanceResponse toBalanceResponse(LeaveType t, LeaveBalance b) {
    BigDecimal remaining = b.getInitialBalance().add(b.getCarryOver())
        .subtract(b.getUsed()).subtract(b.getPending());
    return new LeaveBalanceResponse(t.getId(), t.getCode(), t.getName(), b.getYear(),
        b.getInitialBalance(), b.getCarryOver(), b.getUsed(), b.getPending(), remaining);
  }

  private LeaveRequestResponse toRequestResponse(LeaveRequest lr, LeaveType type) {
    return new LeaveRequestResponse(lr.getId(), lr.getEmployeeId(), lr.getLeaveTypeId(),
        type == null ? null : type.getName(), lr.getStartDate(), lr.getEndDate(),
        lr.getTotalDays(), lr.getReason(), lr.getStatus());
  }
}
