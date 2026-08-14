package com.hirevo.payroll.domain.repository;

import com.hirevo.payroll.domain.model.EmployeeSalaryStructure;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeSalaryStructureRepository extends JpaRepository<EmployeeSalaryStructure, UUID> {

  List<EmployeeSalaryStructure> findByEmployeeIdOrderByEffectiveFromDesc(UUID employeeId);

  @Query("""
      SELECT s FROM EmployeeSalaryStructure s
      WHERE s.employeeId = :employeeId
        AND s.effectiveFrom <= :asOf
        AND (s.effectiveTo IS NULL OR s.effectiveTo >= :asOf)
      """)
  List<EmployeeSalaryStructure> findActiveForEmployee(
      @Param("employeeId") UUID employeeId, @Param("asOf") LocalDate asOf);
}
