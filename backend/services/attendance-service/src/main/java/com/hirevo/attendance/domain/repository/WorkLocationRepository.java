package com.hirevo.attendance.domain.repository;

import com.hirevo.attendance.domain.model.WorkLocation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkLocationRepository extends JpaRepository<WorkLocation, UUID> {
  List<WorkLocation> findByActiveTrue();
}
