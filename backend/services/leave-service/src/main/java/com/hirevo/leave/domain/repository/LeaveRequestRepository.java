package com.hirevo.leave.domain.repository;

import com.hirevo.leave.domain.model.LeaveRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
  List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);
  List<LeaveRequest> findAllByOrderByCreatedAtDesc();
  List<LeaveRequest> findByStatusOrderByCreatedAtDesc(String status);
}
