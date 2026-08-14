package com.hirevo.employee.application;

import com.hirevo.audit.Audited;
import com.hirevo.core.exception.NotFoundException;
import com.hirevo.employee.api.dto.CreateEmployeeRequest;
import com.hirevo.employee.api.dto.EmployeeDetail;
import com.hirevo.employee.api.dto.EmployeePage;
import com.hirevo.employee.api.dto.EmployeeSummary;
import com.hirevo.employee.api.dto.ResignRequest;
import com.hirevo.employee.api.dto.UpdateEmployeeRequest;
import com.hirevo.employee.domain.model.Company;
import com.hirevo.employee.domain.model.Employee;
import com.hirevo.employee.domain.model.EmploymentContract;
import com.hirevo.employee.domain.repository.CompanyRepository;
import com.hirevo.employee.domain.repository.EmployeeRepository;
import com.hirevo.employee.domain.repository.EmploymentContractRepository;
import com.hirevo.security.crypto.FieldEncryptor;
import com.hirevo.tenant.TenantContext;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

  private final EmployeeRepository employees;
  private final EmploymentContractRepository contracts;
  private final CompanyRepository companies;
  private final FieldEncryptor encryptor;

  public EmployeeService(EmployeeRepository employees, EmploymentContractRepository contracts,
                         CompanyRepository companies, FieldEncryptor encryptor) {
    this.employees = employees;
    this.contracts = contracts;
    this.companies = companies;
    this.encryptor = encryptor;
  }

  @Transactional
  @Audited(module = "employee", action = "create", entityType = "employee",
           entityIdExpression = "#result.id()")
  public EmployeeDetail create(CreateEmployeeRequest req) {
    UUID tenantId = TenantContext.getRequired();

    if (employees.existsByTenantIdAndEmployeeNo(tenantId, req.employeeNo())) {
      throw new com.hirevo.core.exception.BusinessException(
          "Employee number '" + req.employeeNo() + "' already in use");
    }

    Company company = companies.findFirstByTenantIdAndDeletedAtIsNull(tenantId)
        .orElseGet(() -> provisionDefaultCompany(tenantId));

    Employee e = new Employee();
    e.setTenantId(tenantId);
    e.setEmployeeNo(req.employeeNo());
    e.setFullName(req.fullName());
    if (req.nik() != null && !req.nik().isBlank()) {
      e.setNikEncrypted(encryptor.encrypt(req.nik().getBytes(StandardCharsets.UTF_8)));
    }
    if (req.npwp() != null && !req.npwp().isBlank()) {
      e.setNpwpEncrypted(encryptor.encrypt(req.npwp().getBytes(StandardCharsets.UTF_8)));
    }
    e.setDateOfBirth(req.dateOfBirth());
    e.setGender(req.gender());
    e.setMaritalStatus(req.maritalStatus());
    e.setPersonalEmail(req.personalEmail());
    e.setPhone(req.phone());
    e.setAddress(req.address());
    e.setHireDate(req.hireDate());
    e.setStatus("active");
    employees.save(e);

    EmploymentContract c = new EmploymentContract();
    c.setTenantId(tenantId);
    c.setEmployeeId(e.getId());
    c.setCompanyId(company.getId());
    c.setContractType(req.contract().contractType());
    c.setStartDate(req.contract().startDate());
    c.setEndDate(req.contract().endDate());
    c.setBaseSalary(req.contract().baseSalary());
    c.setWorkArrangement(req.contract().workArrangement());
    c.setStatus("active");
    contracts.save(c);

    return toDetail(e, c);
  }

  private Company provisionDefaultCompany(UUID tenantId) {
    Company c = new Company();
    c.setTenantId(tenantId);
    c.setLegalName("Perusahaan Utama");
    return companies.save(c);
  }

  @Transactional(readOnly = true)
  public EmployeePage list(String status, String search, int page, int size) {
    Page<Employee> result = employees.search(
        blankToNull(status), blankToNull(search),
        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

    var data = result.getContent().stream().map(e -> {
      String contractType = contracts
          .findFirstByEmployeeIdAndStatusOrderByStartDateDesc(e.getId(), "active")
          .map(EmploymentContract::getContractType)
          .orElse(null);
      return new EmployeeSummary(e.getId(), e.getEmployeeNo(), e.getFullName(), e.getStatus(),
          e.getHireDate(), contractType, e.getPersonalEmail(), e.getPhone());
    }).toList();

    return new EmployeePage(data, new EmployeePage.PageInfo(
        page, size, result.getTotalElements(), result.getTotalPages()));
  }

  @Transactional(readOnly = true)
  public EmployeeDetail get(UUID id) {
    Employee e = employees.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new NotFoundException("Employee", id));
    EmploymentContract c = contracts
        .findFirstByEmployeeIdAndStatusOrderByStartDateDesc(id, "active")
        .orElse(null);
    return toDetail(e, c);
  }

  @Transactional
  @Audited(module = "employee", action = "update", entityType = "employee",
           entityIdExpression = "#id")
  public EmployeeDetail update(UUID id, UpdateEmployeeRequest req) {
    Employee e = employees.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new NotFoundException("Employee", id));
    if (req.fullName() != null) e.setFullName(req.fullName());
    if (req.phone() != null) e.setPhone(req.phone());
    if (req.personalEmail() != null) e.setPersonalEmail(req.personalEmail());
    if (req.address() != null) e.setAddress(req.address());
    e.setUpdatedAt(Instant.now());
    employees.save(e);
    EmploymentContract c = contracts
        .findFirstByEmployeeIdAndStatusOrderByStartDateDesc(id, "active")
        .orElse(null);
    return toDetail(e, c);
  }

  @Transactional
  @Audited(module = "employee", action = "resign", entityType = "employee",
           entityIdExpression = "#id")
  public void resign(UUID id, ResignRequest req) {
    Employee e = employees.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new NotFoundException("Employee", id));
    e.setResignDate(req.resignDate());
    e.setResignReason(req.reason());
    e.setStatus("resigned");
    e.setUpdatedAt(Instant.now());
    employees.save(e);
  }

  private EmployeeDetail toDetail(Employee e, EmploymentContract c) {
    String nikMasked = e.getNikEncrypted() == null ? null
        : FieldEncryptor.maskKeepLast(
            new String(encryptor.decrypt(e.getNikEncrypted()), StandardCharsets.UTF_8), 4);
    String npwpMasked = e.getNpwpEncrypted() == null ? null
        : FieldEncryptor.maskKeepLast(
            new String(encryptor.decrypt(e.getNpwpEncrypted()), StandardCharsets.UTF_8), 4);

    EmployeeDetail.ContractInfo contractInfo = c == null ? null : new EmployeeDetail.ContractInfo(
        c.getId(), c.getContractType(), c.getStartDate(), c.getEndDate(),
        c.getBaseSalary(), c.getWorkArrangement(), c.getStatus());

    return new EmployeeDetail(
        e.getId(), e.getEmployeeNo(), e.getFullName(), nikMasked, npwpMasked,
        e.getDateOfBirth(), e.getGender(), e.getMaritalStatus(), e.getPersonalEmail(),
        e.getPhone(), e.getAddress(), e.getHireDate(), e.getResignDate(), e.getStatus(),
        contractInfo);
  }

  private static String blankToNull(String s) {
    return (s == null || s.isBlank()) ? null : s;
  }
}
