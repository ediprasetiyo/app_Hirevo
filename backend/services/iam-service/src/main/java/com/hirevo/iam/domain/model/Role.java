package com.hirevo.iam.domain.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles", schema = "iam")
public class Role {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id")
  private UUID tenantId; // null = system role

  @Column(nullable = false, length = 50)
  private String name;

  @Column(columnDefinition = "text")
  private String description;

  @Column(name = "is_system", nullable = false)
  private boolean system = false;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "role_permissions", schema = "iam",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private Set<Permission> permissions = new HashSet<>();

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public boolean isSystem() { return system; }
  public void setSystem(boolean system) { this.system = system; }
  public Set<Permission> getPermissions() { return permissions; }
  public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }
}
