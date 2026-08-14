package com.hirevo.attendance.domain.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite PK matching attendance_logs' PRIMARY KEY (id, work_date) —
 * required because Postgres range-partitioned tables must include the
 * partition key (work_date) in every unique constraint, including the PK.
 */
public class AttendanceLogId implements Serializable {
  private UUID id;
  private LocalDate workDate;

  public AttendanceLogId() {}

  public AttendanceLogId(UUID id, LocalDate workDate) {
    this.id = id;
    this.workDate = workDate;
  }

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public LocalDate getWorkDate() { return workDate; }
  public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AttendanceLogId that)) return false;
    return Objects.equals(id, that.id) && Objects.equals(workDate, that.workDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, workDate);
  }
}
