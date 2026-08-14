package com.hirevo.leave.domain.repository;

import com.hirevo.leave.domain.model.LeaveBalance;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {
  Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(UUID employeeId, UUID leaveTypeId, Integer year);
  List<LeaveBalance> findByEmployeeIdAndYear(UUID employeeId, Integer year);
}
