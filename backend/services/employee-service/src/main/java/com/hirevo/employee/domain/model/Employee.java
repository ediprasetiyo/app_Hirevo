package com.hirevo.employee.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees", schema = "employee")
public class Employee {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "employee_no", nullable = false)
  private String employeeNo;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  private String nickname;

  @Column(name = "nik_encrypted")
  private byte[] nikEncrypted;

  @Column(name = "npwp_encrypted")
  private byte[] npwpEncrypted;

  @Column(name = "passport_no")
  private String passportNo;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Column(name = "place_of_birth")
  private String placeOfBirth;

  private String gender;

  @Column(name = "marital_status")
  private String maritalStatus;

  private String religion;

  @Column(name = "blood_type")
  private String bloodType;

  private String nationality = "Indonesia";

  @Column(name = "personal_email")
  private String personalEmail;

  private String phone;
  private String whatsapp;

  @Column(columnDefinition = "text")
  private String address;

  @Column(name = "city_code")
  private String cityCode;

  @Column(name = "province_code")
  private String provinceCode;

  @Column(name = "postal_code")
  private String postalCode;

  @Column(name = "photo_url", columnDefinition = "text")
  private String photoUrl;

  @Column(name = "manager_id")
  private UUID managerId;

  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;

  @Column(name = "resign_date")
  private LocalDate resignDate;

  @Column(name = "resign_reason", columnDefinition = "text")
  private String resignReason;

  @Column(nullable = false)
  private String status = "active";

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @Column(name = "deleted_at")
  private Instant deletedAt;

  public UUID getId() { return id; }
  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
  public String getEmployeeNo() { return employeeNo; }
  public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  public String getNickname() { return nickname; }
  public void setNickname(String nickname) { this.nickname = nickname; }
  public byte[] getNikEncrypted() { return nikEncrypted; }
  public void setNikEncrypted(byte[] nikEncrypted) { this.nikEncrypted = nikEncrypted; }
  public byte[] getNpwpEncrypted() { return npwpEncrypted; }
  public void setNpwpEncrypted(byte[] npwpEncrypted) { this.npwpEncrypted = npwpEncrypted; }
  public String getPassportNo() { return passportNo; }
  public void setPassportNo(String passportNo) { this.passportNo = passportNo; }
  public LocalDate getDateOfBirth() { return dateOfBirth; }
  public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
  public String getPlaceOfBirth() { return placeOfBirth; }
  public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth; }
  public String getGender() { return gender; }
  public void setGender(String gender) { this.gender = gender; }
  public String getMaritalStatus() { return maritalStatus; }
  public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
  public String getReligion() { return religion; }
  public void setReligion(String religion) { this.religion = religion; }
  public String getBloodType() { return bloodType; }
  public void setBloodType(String bloodType) { this.bloodType = bloodType; }
  public String getNationality() { return nationality; }
  public void setNationality(String nationality) { this.nationality = nationality; }
  public String getPersonalEmail() { return personalEmail; }
  public void setPersonalEmail(String personalEmail) { this.personalEmail = personalEmail; }
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }
  public String getWhatsapp() { return whatsapp; }
  public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }
  public String getCityCode() { return cityCode; }
  public void setCityCode(String cityCode) { this.cityCode = cityCode; }
  public String getProvinceCode() { return provinceCode; }
  public void setProvinceCode(String provinceCode) { this.provinceCode = provinceCode; }
  public String getPostalCode() { return postalCode; }
  public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
  public String getPhotoUrl() { return photoUrl; }
  public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
  public UUID getManagerId() { return managerId; }
  public void setManagerId(UUID managerId) { this.managerId = managerId; }
  public LocalDate getHireDate() { return hireDate; }
  public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
  public LocalDate getResignDate() { return resignDate; }
  public void setResignDate(LocalDate resignDate) { this.resignDate = resignDate; }
  public String getResignReason() { return resignReason; }
  public void setResignReason(String resignReason) { this.resignReason = resignReason; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
  public Instant getDeletedAt() { return deletedAt; }
  public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
