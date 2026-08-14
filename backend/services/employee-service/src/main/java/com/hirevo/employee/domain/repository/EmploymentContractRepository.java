package com.hirevo.employee.domain.repository;

import com.hirevo.employee.domain.model.EmploymentContract;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmploymentContractRepository extends JpaRepository<EmploymentContract, UUID> {
  Optional<EmploymentContract> findFirstByEmployeeIdAndStatusOrderByStartDateDesc(
      UUID employeeId, String status);

  List<EmploymentContract> findByEmployeeId(UUID employeeId);
}
