package com.hirevo.iam.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions", schema = "iam")
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, length = 100)
  private String code;

  @Column(nullable = false, length = 50)
  private String module;

  @Column(columnDefinition = "text")
  private String description;

  public Integer getId() { return id; }
  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }
  public String getModule() { return module; }
  public void setModule(String module) { this.module = module; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
}
