package com.hirevo.reimbursement.application;

import com.hirevo.audit.Audited;
import com.hirevo.core.exception.BusinessException;
import com.hirevo.core.exception.NotFoundException;
import com.hirevo.reimbursement.api.dto.ReimbursementDtos.CategoryResponse;
import com.hirevo.reimbursement.api.dto.ReimbursementDtos.CreateRequest;
import com.hirevo.reimbursement.api.dto.ReimbursementDtos.ItemInput;
import com.hirevo.reimbursement.api.dto.ReimbursementDtos.ItemResponse;
import com.hirevo.reimbursement.api.dto.ReimbursementDtos.RequestResponse;
import com.hirevo.reimbursement.domain.model.ReimbursementCategory;
import com.hirevo.reimbursement.domain.model.ReimbursementItem;
import com.hirevo.reimbursement.domain.model.ReimbursementRequest;
import com.hirevo.reimbursement.domain.repository.ReimbursementCategoryRepository;
import com.hirevo.reimbursement.domain.repository.ReimbursementItemRepository;
import com.hirevo.reimbursement.domain.repository.ReimbursementRequestRepository;
import com.hirevo.tenant.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Expense reimbursement categories, requests + line items.
 *
 * <p>Scope simplifications (MVP, documented not hidden): the schema also
 * provisions OCR receipt-scanning ({@code ocr_jobs}), duplicate-receipt
 * fingerprinting, and fraud-signal tables — none of that is implemented here
 * (no OCR provider is wired up in this environment). {@code fraud_score} and
 * {@code review_status} stay at their DB defaults; every submitted request
 * goes through a plain single-step manual approval, same simplification
 * leave-service and payroll-service already document for their own
 * approval flows.
 */
@Service
public class ReimbursementService {

  private final ReimbursementCategoryRepository categories;
  private final ReimbursementRequestRepository requests;
  private final ReimbursementItemRepository items;

  public ReimbursementService(
      ReimbursementCategoryRepository categories, ReimbursementRequestRepository requests,
      ReimbursementItemRepository items) {
    this.categories = categories;
    this.requests = requests;
    this.items = items;
  }

  private record DefaultCategory(String code, String name, BigDecimal monthlyLimit, boolean requireReceipt) {}

  private static final List<DefaultCategory> DEFAULT_CATEGORIES = List.of(
      new DefaultCategory("TRANSPORT", "Transportasi", new BigDecimal("1500000"), true),
      new DefaultCategory("MEALS", "Makan", new BigDecimal("1000000"), true),
      new DefaultCategory("MEDICAL", "Kesehatan", new BigDecimal("2000000"), true),
      new DefaultCategory("OFFICE", "Perlengkapan Kantor", new BigDecimal("500000"), true),
      new DefaultCategory("OTHER", "Lainnya", null, true)
  );

  @Transactional
  public List<CategoryResponse> listCategories() {
    UUID tenantId = TenantContext.getRequired();
    if (!categories.existsByTenantId(tenantId)) {
      for (DefaultCategory dc : DEFAULT_CATEGORIES) {
        ReimbursementCategory c = new ReimbursementCategory();
        c.setTenantId(tenantId);
        c.setCode(dc.code());
        c.setName(dc.name());
        c.setMonthlyLimit(dc.monthlyLimit());
        c.setRequireReceipt(dc.requireReceipt());
        categories.save(c);
      }
    }
    return categories.findByTenantIdAndActiveTrue(tenantId).stream().map(this::toCategoryResponse).toList();
  }

  @Transactional
  @Audited(module = "reimbursement", action = "submit", entityType = "reimbursement_request",
           entityIdExpression = "#result.id()")
  public RequestResponse submit(CreateRequest req) {
    BigDecimal total = req.items().stream().map(ItemInput::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (total.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessException("Total pengajuan harus lebih dari 0");
    }

    ReimbursementRequest r = new ReimbursementRequest();
    r.setTenantId(TenantContext.getRequired());
    r.setEmployeeId(req.employeeId());
    r.setTitle(req.title());
    r.setDescription(req.description());
    r.setTotalAmount(total);
    r.setStatus("submitted");
    requests.save(r);

    for (ItemInput itemInput : req.items()) {
      categories.findById(itemInput.categoryId())
          .orElseThrow(() -> new NotFoundException("ReimbursementCategory", itemInput.categoryId()));
      ReimbursementItem item = new ReimbursementItem();
      item.setTenantId(r.getTenantId());
      item.setReimbursementRequestId(r.getId());
      item.setCategoryId(itemInput.categoryId());
      item.setTransactionDate(itemInput.transactionDate());
      item.setDescription(itemInput.description());
      item.setAmount(itemInput.amount());
      items.save(item);
    }

    return toRequestResponse(r);
  }

  @Transactional
  @Audited(module = "reimbursement", action = "approve", entityType = "reimbursement_request", entityIdExpression = "#id")
  public RequestResponse approve(UUID id) {
    ReimbursementRequest r = requests.findById(id).orElseThrow(() -> new NotFoundException("ReimbursementRequest", id));
    if (!"submitted".equals(r.getStatus())) {
      throw new BusinessException("Hanya pengajuan berstatus submitted yang bisa disetujui");
    }
    r.setStatus("approved");
    r.setUpdatedAt(Instant.now());
    requests.save(r);
    return toRequestResponse(r);
  }

  @Transactional
  @Audited(module = "reimbursement", action = "reject", entityType = "reimbursement_request", entityIdExpression = "#id")
  public RequestResponse reject(UUID id) {
    ReimbursementRequest r = requests.findById(id).orElseThrow(() -> new NotFoundException("ReimbursementRequest", id));
    if (!"submitted".equals(r.getStatus())) {
      throw new BusinessException("Hanya pengajuan berstatus submitted yang bisa ditolak");
    }
    r.setStatus("rejected");
    r.setUpdatedAt(Instant.now());
    requests.save(r);
    return toRequestResponse(r);
  }

  @Transactional
  @Audited(module = "reimbursement", action = "mark_paid", entityType = "reimbursement_request", entityIdExpression = "#id")
  public RequestResponse markPaid(UUID id) {
    ReimbursementRequest r = requests.findById(id).orElseThrow(() -> new NotFoundException("ReimbursementRequest", id));
    if (!"approved".equals(r.getStatus())) {
      throw new BusinessException("Hanya pengajuan berstatus approved yang bisa ditandai dibayar");
    }
    r.setStatus("paid");
    r.setPaidAt(Instant.now());
    r.setUpdatedAt(Instant.now());
    requests.save(r);
    return toRequestResponse(r);
  }

  @Transactional(readOnly = true)
  public List<RequestResponse> listRequests(UUID employeeId) {
    List<ReimbursementRequest> list = employeeId == null
        ? requests.findAllByOrderByCreatedAtDesc()
        : requests.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    return list.stream().map(this::toRequestResponse).toList();
  }

  private RequestResponse toRequestResponse(ReimbursementRequest r) {
    Map<UUID, ReimbursementCategory> categoryById = categories.findAll().stream()
        .collect(java.util.stream.Collectors.toMap(ReimbursementCategory::getId, c -> c));
    List<ItemResponse> itemResponses = items.findByReimbursementRequestId(r.getId()).stream()
        .map(i -> {
          ReimbursementCategory c = categoryById.get(i.getCategoryId());
          return new ItemResponse(i.getId(), i.getCategoryId(), c == null ? null : c.getName(),
              i.getTransactionDate(), i.getDescription(), i.getAmount());
        }).toList();
    return new RequestResponse(r.getId(), r.getEmployeeId(), r.getRequestNo(), r.getTitle(),
        r.getDescription(), r.getTotalAmount(), r.getStatus(), itemResponses, r.getPaidAt(), r.getCreatedAt());
  }

  private CategoryResponse toCategoryResponse(ReimbursementCategory c) {
    return new CategoryResponse(c.getId(), c.getCode(), c.getName(), c.getMonthlyLimit(),
        c.getYearlyLimit(), c.isRequireReceipt(), c.isTaxable());
  }
}
