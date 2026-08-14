package com.hirevo.payroll.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hirevo.audit.Audited;
import com.hirevo.core.exception.BusinessException;
import com.hirevo.core.exception.NotFoundException;
import com.hirevo.payroll.api.dto.PayrollPeriodDtos.CreatePayrollPeriodRequest;
import com.hirevo.payroll.api.dto.PayrollPeriodDtos.PayrollPeriodResponse;
import com.hirevo.payroll.api.dto.PayrollRunDtos.PayrollRunResponse;
import com.hirevo.payroll.api.dto.PayslipDtos.PayslipResponse;
import com.hirevo.payroll.api.dto.SalaryComponentDtos.CreateSalaryComponentRequest;
import com.hirevo.payroll.api.dto.SalaryComponentDtos.SalaryComponentResponse;
import com.hirevo.payroll.api.dto.SalaryStructureDtos.CreateSalaryStructureRequest;
import com.hirevo.payroll.api.dto.SalaryStructureDtos.SalaryStructureResponse;
import com.hirevo.payroll.domain.model.EmployeeSalaryStructure;
import com.hirevo.payroll.domain.model.PayrollPeriod;
import com.hirevo.payroll.domain.model.PayrollRun;
import com.hirevo.payroll.domain.model.Payslip;
import com.hirevo.payroll.domain.model.SalaryComponent;
import com.hirevo.payroll.domain.repository.EmployeeSalaryStructureRepository;
import com.hirevo.payroll.domain.repository.PayrollPeriodRepository;
import com.hirevo.payroll.domain.repository.PayrollRunRepository;
import com.hirevo.payroll.domain.repository.PayslipRepository;
import com.hirevo.payroll.domain.repository.SalaryComponentRepository;
import com.hirevo.payroll.infrastructure.client.EmployeeServiceClient;
import com.hirevo.payroll.infrastructure.client.EmployeeServiceClient.EmployeeInfo;
import com.hirevo.tenant.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Salary components, periods, runs, and payslip calculation.
 *
 * <p>See {@link PayrollCalculations} for the documented BPJS/PPh21 MVP
 * simplifications. Two more scope simplifications specific to orchestration:
 * <ul>
 *   <li>{@code company_id} on {@code payroll_runs} has no FK to a real
 *       company record (employee-service doesn't expose a company-lookup
 *       endpoint yet) — the tenant id is reused as a stable per-tenant
 *       company id placeholder.</li>
 *   <li>Approval is single-step (no separate review step before approve),
 *       same simplification leave-service already documents for its own
 *       approval flow — the {@code reviewed_by}/{@code reviewed_at} columns
 *       stay null, which trivially satisfies the DB's four-eyes check
 *       constraint ({@code approved_by <> reviewed_by} only applies when
 *       both are set).</li>
 * </ul>
 */
@Service
public class PayrollService {

  private final SalaryComponentRepository components;
  private final EmployeeSalaryStructureRepository structures;
  private final PayrollPeriodRepository periods;
  private final PayrollRunRepository runs;
  private final PayslipRepository payslips;
  private final EmployeeServiceClient employeeClient;
  private final ObjectMapper objectMapper;

  public PayrollService(
      SalaryComponentRepository components, EmployeeSalaryStructureRepository structures,
      PayrollPeriodRepository periods, PayrollRunRepository runs, PayslipRepository payslips,
      EmployeeServiceClient employeeClient, ObjectMapper objectMapper) {
    this.components = components;
    this.structures = structures;
    this.periods = periods;
    this.runs = runs;
    this.payslips = payslips;
    this.employeeClient = employeeClient;
    this.objectMapper = objectMapper;
  }

  private record DefaultComponent(String code, String name, String category, boolean taxable) {}

  private static final List<DefaultComponent> DEFAULT_COMPONENTS = List.of(
      new DefaultComponent("TRANSPORT", "Tunjangan Transport", "earning", true),
      new DefaultComponent("MEAL", "Tunjangan Makan", "earning", true),
      new DefaultComponent("POSITION", "Tunjangan Jabatan", "earning", true),
      new DefaultComponent("COMMUNICATION", "Tunjangan Komunikasi", "earning", false)
  );

  @Transactional
  public List<SalaryComponentResponse> listComponents() {
    UUID tenantId = TenantContext.getRequired();
    if (!components.existsByTenantId(tenantId)) {
      for (DefaultComponent dc : DEFAULT_COMPONENTS) {
        SalaryComponent c = new SalaryComponent();
        c.setTenantId(tenantId);
        c.setCode(dc.code());
        c.setName(dc.name());
        c.setCategory(dc.category());
        c.setTaxable(dc.taxable());
        c.setBpjsKesBase(true);
        c.setBpjsTkBase(true);
        components.save(c);
      }
    }
    return components.findByTenantIdAndActiveTrueOrderByDisplayOrder(tenantId).stream()
        .map(this::toComponentResponse).toList();
  }

  @Transactional
  public SalaryComponentResponse createComponent(CreateSalaryComponentRequest req) {
    SalaryComponent c = new SalaryComponent();
    c.setTenantId(TenantContext.getRequired());
    c.setCode(req.code().toUpperCase());
    c.setName(req.name());
    c.setCategory(req.category());
    c.setDefaultAmount(req.defaultAmount() == null ? BigDecimal.ZERO : req.defaultAmount());
    c.setTaxable(req.taxable() == null || req.taxable());
    c.setBpjsKesBase(Boolean.TRUE.equals(req.bpjsKesBase()));
    c.setBpjsTkBase(Boolean.TRUE.equals(req.bpjsTkBase()));
    components.save(c);
    return toComponentResponse(c);
  }

  @Transactional(readOnly = true)
  public List<SalaryStructureResponse> listStructures(UUID employeeId) {
    return structures.findByEmployeeIdOrderByEffectiveFromDesc(employeeId).stream()
        .map(this::toStructureResponse).toList();
  }

  @Transactional
  public SalaryStructureResponse assignStructure(CreateSalaryStructureRequest req) {
    SalaryComponent component = components.findById(req.salaryComponentId())
        .orElseThrow(() -> new NotFoundException("SalaryComponent", req.salaryComponentId()));
    EmployeeSalaryStructure s = new EmployeeSalaryStructure();
    s.setTenantId(TenantContext.getRequired());
    s.setEmployeeId(req.employeeId());
    s.setSalaryComponentId(req.salaryComponentId());
    s.setAmount(req.amount());
    s.setEffectiveFrom(req.effectiveFrom());
    s.setReason(req.reason());
    structures.save(s);
    return toStructureResponse(s, component);
  }

  @Transactional
  public List<PayrollPeriodResponse> listPeriods() {
    return periods.findByTenantIdOrderByStartDateDesc(TenantContext.getRequired()).stream()
        .map(this::toPeriodResponse).toList();
  }

  @Transactional
  public PayrollPeriodResponse createPeriod(CreatePayrollPeriodRequest req) {
    UUID tenantId = TenantContext.getRequired();
    String type = req.type() == null ? "monthly" : req.type();
    if (periods.findByTenantIdAndPeriodYearAndPeriodMonthAndType(tenantId, req.year(), req.month(), type)
        .isPresent()) {
      throw new BusinessException("Periode untuk bulan ini sudah ada");
    }
    YearMonth ym = YearMonth.of(req.year(), req.month());
    PayrollPeriod p = new PayrollPeriod();
    p.setTenantId(tenantId);
    p.setPeriodYear(req.year());
    p.setPeriodMonth(req.month());
    p.setStartDate(ym.atDay(1));
    p.setEndDate(ym.atEndOfMonth());
    p.setCutoffDate(ym.atDay(Math.min(25, ym.lengthOfMonth())));
    p.setPayDate(ym.atEndOfMonth());
    p.setType(type);
    String monthName = ym.getMonth().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
    p.setName("Payroll " + monthName + " " + req.year());
    periods.save(p);
    return toPeriodResponse(p);
  }

  @Transactional
  public List<PayrollRunResponse> listRuns() {
    return runs.findByTenantIdOrderByCreatedAtDesc(TenantContext.getRequired()).stream()
        .map(this::toRunResponse).toList();
  }

  @Transactional
  public PayrollRunResponse createRun(UUID payrollPeriodId) {
    PayrollPeriod period = periods.findById(payrollPeriodId)
        .orElseThrow(() -> new NotFoundException("PayrollPeriod", payrollPeriodId));
    PayrollRun run = new PayrollRun();
    run.setTenantId(TenantContext.getRequired());
    run.setPayrollPeriodId(period.getId());
    run.setCompanyId(TenantContext.getRequired());
    run.setRunNo(period.getPeriodYear() + "-" + String.format("%02d", period.getPeriodMonth()));
    runs.save(run);
    return toRunResponse(run, period);
  }

  @Transactional
  @Audited(module = "payroll", action = "calculate", entityType = "payroll_run", entityIdExpression = "#id")
  public PayrollRunResponse calculateRun(UUID id) {
    PayrollRun run = runs.findById(id).orElseThrow(() -> new NotFoundException("PayrollRun", id));
    if ("paid".equals(run.getStatus()) || "approved".equals(run.getStatus())) {
      throw new BusinessException("Run yang sudah approved/paid tidak bisa dihitung ulang");
    }
    PayrollPeriod period = periods.findById(run.getPayrollPeriodId())
        .orElseThrow(() -> new NotFoundException("PayrollPeriod", run.getPayrollPeriodId()));

    payslips.deleteByPayrollRunId(id);

    List<EmployeeServiceClient.SummaryRow> activeEmployees = employeeClient.listActiveEmployees();

    BigDecimal totalGross = BigDecimal.ZERO;
    BigDecimal totalDeductions = BigDecimal.ZERO;
    BigDecimal totalPph21 = BigDecimal.ZERO;
    BigDecimal totalBpjsEmployee = BigDecimal.ZERO;
    BigDecimal totalBpjsEmployer = BigDecimal.ZERO;
    BigDecimal totalNet = BigDecimal.ZERO;
    int count = 0;

    for (EmployeeServiceClient.SummaryRow row : activeEmployees) {
      EmployeeInfo emp = employeeClient.getEmployee(row.id());
      if (emp.activeContract() == null || emp.activeContract().baseSalary() == null) {
        continue; // no active contract — nothing to pay
      }
      Payslip slip = calculateOne(run, period, emp);
      payslips.save(slip);
      count++;
      totalGross = totalGross.add(slip.getGrossAmount());
      totalDeductions = totalDeductions.add(slip.getPph21Amount())
          .add(slip.getBpjsEmployee()).add(slip.getOtherDeductions());
      totalPph21 = totalPph21.add(slip.getPph21Amount());
      totalBpjsEmployee = totalBpjsEmployee.add(slip.getBpjsEmployee());
      totalBpjsEmployer = totalBpjsEmployer.add(slip.getBpjsEmployer());
      totalNet = totalNet.add(slip.getNetAmount());
    }

    run.setStatus("calculated");
    run.setTotalEmployees(count);
    run.setTotalGross(totalGross);
    run.setTotalDeductions(totalDeductions);
    run.setTotalPph21(totalPph21);
    run.setTotalBpjsEmployee(totalBpjsEmployee);
    run.setTotalBpjsEmployer(totalBpjsEmployer);
    run.setTotalNet(totalNet);
    run.setCalculatedAt(Instant.now());
    run.setUpdatedAt(Instant.now());
    runs.save(run);
    return toRunResponse(run, period);
  }

  private Payslip calculateOne(PayrollRun run, PayrollPeriod period, EmployeeInfo emp) {
    BigDecimal baseSalary = emp.activeContract().baseSalary();
    List<EmployeeSalaryStructure> extras = structures.findActiveForEmployee(emp.id(), period.getStartDate());

    List<Map<String, Object>> lineItems = new ArrayList<>();
    lineItems.add(Map.of("code", "BASIC", "name", "Gaji Pokok", "category", "earning",
        "amount", baseSalary, "taxable", true, "bpjsBase", true));

    BigDecimal gross = baseSalary;
    BigDecimal taxableAmount = baseSalary;
    BigDecimal bpjsKesBase = baseSalary;
    BigDecimal bpjsTkBase = baseSalary;

    for (EmployeeSalaryStructure extra : extras) {
      SalaryComponent comp = components.findById(extra.getSalaryComponentId()).orElse(null);
      if (comp == null) continue;
      BigDecimal amount = extra.getAmount();
      lineItems.add(Map.of("code", comp.getCode(), "name", comp.getName(),
          "category", comp.getCategory(), "amount", amount,
          "taxable", comp.isTaxable(), "bpjsBase", comp.isBpjsKesBase()));
      if ("earning".equals(comp.getCategory()) || "benefit".equals(comp.getCategory())) {
        gross = gross.add(amount);
        if (comp.isTaxable()) taxableAmount = taxableAmount.add(amount);
        if (comp.isBpjsKesBase()) bpjsKesBase = bpjsKesBase.add(amount);
        if (comp.isBpjsTkBase()) bpjsTkBase = bpjsTkBase.add(amount);
      }
    }

    PayrollCalculations.BpjsResult kes = PayrollCalculations.bpjsKesehatan(bpjsKesBase);
    PayrollCalculations.BpjsResult tk = PayrollCalculations.bpjsKetenagakerjaan(bpjsTkBase);
    BigDecimal bpjsEmployee = kes.employeeAmount().add(tk.employeeAmount());
    BigDecimal bpjsEmployer = kes.employerAmount().add(tk.employerAmount());

    String terCategory = PayrollCalculations.terCategory(emp.maritalStatus());
    BigDecimal pph21Base = taxableAmount.subtract(bpjsEmployee).max(BigDecimal.ZERO);
    BigDecimal pph21 = PayrollCalculations.pph21(pph21Base, terCategory);

    BigDecimal net = gross.subtract(bpjsEmployee).subtract(pph21);

    Payslip slip = new Payslip();
    slip.setTenantId(TenantContext.getRequired());
    slip.setPayrollRunId(run.getId());
    slip.setEmployeeId(emp.id());
    slip.setGrossAmount(gross);
    slip.setTaxableAmount(taxableAmount);
    slip.setPph21Amount(pph21);
    slip.setBpjsEmployee(bpjsEmployee);
    slip.setBpjsEmployer(bpjsEmployer);
    slip.setNetAmount(net);
    slip.setTerCategory(terCategory);
    slip.setPtkpCode("married".equalsIgnoreCase(emp.maritalStatus()) ? "K/0" : "TK/0");
    slip.setCalculationSnapshot(writeSnapshot(lineItems, kes, tk));
    return slip;
  }

  private String writeSnapshot(
      List<Map<String, Object>> lineItems, PayrollCalculations.BpjsResult kes,
      PayrollCalculations.BpjsResult tk) {
    try {
      return objectMapper.writeValueAsString(Map.of(
          "components", lineItems,
          "bpjsKesehatan", Map.of("employee", kes.employeeAmount(), "employer", kes.employerAmount()),
          "bpjsKetenagakerjaan", Map.of("employee", tk.employeeAmount(), "employer", tk.employerAmount())));
    } catch (Exception e) {
      return "{}";
    }
  }

  @Transactional
  @Audited(module = "payroll", action = "approve", entityType = "payroll_run", entityIdExpression = "#id")
  public PayrollRunResponse approveRun(UUID id, UUID approverId) {
    PayrollRun run = runs.findById(id).orElseThrow(() -> new NotFoundException("PayrollRun", id));
    if (!"calculated".equals(run.getStatus())) {
      throw new BusinessException("Hanya run berstatus calculated yang bisa disetujui");
    }
    run.setStatus("approved");
    run.setApprovedBy(approverId);
    run.setApprovedAt(Instant.now());
    run.setUpdatedAt(Instant.now());
    runs.save(run);
    return toRunResponse(run, periods.findById(run.getPayrollPeriodId()).orElse(null));
  }

  @Transactional
  @Audited(module = "payroll", action = "mark_paid", entityType = "payroll_run", entityIdExpression = "#id")
  public PayrollRunResponse markPaid(UUID id) {
    PayrollRun run = runs.findById(id).orElseThrow(() -> new NotFoundException("PayrollRun", id));
    if (!"approved".equals(run.getStatus())) {
      throw new BusinessException("Hanya run berstatus approved yang bisa ditandai paid");
    }
    run.setStatus("paid");
    run.setPaidAt(Instant.now());
    run.setUpdatedAt(Instant.now());
    runs.save(run);
    return toRunResponse(run, periods.findById(run.getPayrollPeriodId()).orElse(null));
  }

  @Transactional(readOnly = true)
  public List<PayslipResponse> listPayslips(UUID runId) {
    return payslips.findByPayrollRunId(runId).stream().map(this::toPayslipResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<PayslipResponse> listPayslipsForEmployee(UUID employeeId) {
    return payslips.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
        .map(this::toPayslipResponse).toList();
  }

  @Transactional(readOnly = true)
  public PayslipResponse getPayslip(UUID id) {
    return payslips.findById(id).map(this::toPayslipResponse)
        .orElseThrow(() -> new NotFoundException("Payslip", id));
  }

  private SalaryComponentResponse toComponentResponse(SalaryComponent c) {
    return new SalaryComponentResponse(c.getId(), c.getCode(), c.getName(), c.getCategory(),
        c.getType(), c.getDefaultAmount(), c.isTaxable(), c.isBpjsKesBase(), c.isBpjsTkBase());
  }

  private SalaryStructureResponse toStructureResponse(EmployeeSalaryStructure s) {
    SalaryComponent comp = components.findById(s.getSalaryComponentId()).orElse(null);
    return toStructureResponse(s, comp);
  }

  private SalaryStructureResponse toStructureResponse(EmployeeSalaryStructure s, SalaryComponent comp) {
    return new SalaryStructureResponse(s.getId(), s.getEmployeeId(), s.getSalaryComponentId(),
        comp == null ? null : comp.getName(), comp == null ? null : comp.getCategory(),
        s.getAmount(), s.getEffectiveFrom(), s.getEffectiveTo());
  }

  private PayrollPeriodResponse toPeriodResponse(PayrollPeriod p) {
    return new PayrollPeriodResponse(p.getId(), p.getName(), p.getPeriodYear(), p.getPeriodMonth(),
        p.getStartDate(), p.getEndDate(), p.getCutoffDate(), p.getPayDate(), p.getType(), p.getStatus());
  }

  private PayrollRunResponse toRunResponse(PayrollRun r) {
    return toRunResponse(r, periods.findById(r.getPayrollPeriodId()).orElse(null));
  }

  private PayrollRunResponse toRunResponse(PayrollRun r, PayrollPeriod period) {
    return new PayrollRunResponse(r.getId(), r.getPayrollPeriodId(),
        period == null ? null : period.getName(), r.getRunNo(), r.getStatus(),
        r.getTotalEmployees(), r.getTotalGross(), r.getTotalDeductions(), r.getTotalPph21(),
        r.getTotalBpjsEmployee(), r.getTotalBpjsEmployer(), r.getTotalNet(),
        r.getCalculatedAt(), r.getApprovedAt(), r.getPaidAt());
  }

  private PayslipResponse toPayslipResponse(Payslip p) {
    return new PayslipResponse(p.getId(), p.getPayrollRunId(), p.getEmployeeId(), null,
        p.getGrossAmount(), p.getTaxableAmount(), p.getPph21Amount(), p.getBpjsEmployee(),
        p.getBpjsEmployer(), p.getOtherDeductions(), p.getNetAmount(), p.getPtkpCode(),
        p.getTerCategory(), p.getCalculationSnapshot(), p.getCreatedAt());
  }
}
