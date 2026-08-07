package com.hirevo.iam.application;

import com.hirevo.audit.Audited;
import com.hirevo.core.exception.ErrorCode;
import com.hirevo.core.exception.HirevoException;
import com.hirevo.iam.api.dto.TenantSignupRequest;
import com.hirevo.iam.api.dto.TenantSignupResponse;
import com.hirevo.iam.domain.model.Role;
import com.hirevo.iam.domain.model.Tenant;
import com.hirevo.iam.domain.model.User;
import com.hirevo.iam.domain.repository.RoleRepository;
import com.hirevo.iam.domain.repository.TenantRepository;
import com.hirevo.iam.domain.repository.UserRepository;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantSignupService {

  private final TenantRepository tenants;
  private final UserRepository users;
  private final RoleRepository roles;
  private final PasswordEncoder passwordEncoder;

  public TenantSignupService(TenantRepository tenants, UserRepository users,
                             RoleRepository roles, PasswordEncoder passwordEncoder) {
    this.tenants = tenants;
    this.users = users;
    this.roles = roles;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  @Audited(module = "tenant", action = "signup",
           entityType = "tenant", entityIdExpression = "#result.tenantId()")
  public TenantSignupResponse signup(TenantSignupRequest req) {
    if (tenants.existsBySubdomain(req.subdomain())) {
      throw new HirevoException(ErrorCode.TENANT_SUBDOMAIN_TAKEN,
          "Subdomain '" + req.subdomain() + "' is already taken");
    }

    Tenant tenant = new Tenant();
    tenant.setName(req.companyName());
    tenant.setSubdomain(req.subdomain());
    tenant.setPlan("free");
    tenant.setStatus("trial");
    tenant.setTrialEndsAt(LocalDate.now().plusDays(14));
    tenant.setBillingEmail(req.adminEmail());
    tenants.save(tenant);

    Role superAdmin = roles.findBySystemTrueAndName("super_admin")
        .orElseThrow(() -> new IllegalStateException(
            "Missing system role 'super_admin' — seed missing"));

    User admin = new User();
    admin.setTenantId(tenant.getId());
    admin.setEmail(req.adminEmail().toLowerCase());
    admin.setFullName(req.adminFullName());
    admin.setPasswordHash(passwordEncoder.encode(req.adminPassword()));
    admin.setStatus("active");
    admin.getRoles().add(superAdmin);
    users.save(admin);

    return new TenantSignupResponse(
        tenant.getId(),
        tenant.getSubdomain(),
        admin.getId(),
        "https://" + tenant.getSubdomain() + ".hirevo.id");
  }
}
