package com.hirevo.attendance.domain.repository;

import com.hirevo.attendance.domain.model.AttendanceLog;
import com.hirevo.attendance.domain.model.AttendanceLogId;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, AttendanceLogId> {

  Optional<AttendanceLog> findByEmployeeIdAndWorkDate(UUID employeeId, LocalDate workDate);

  List<AttendanceLog> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateDesc(
      UUID employeeId, LocalDate from, LocalDate to);

  List<AttendanceLog> findByWorkDateBetweenOrderByWorkDateDesc(LocalDate from, LocalDate to);
}
