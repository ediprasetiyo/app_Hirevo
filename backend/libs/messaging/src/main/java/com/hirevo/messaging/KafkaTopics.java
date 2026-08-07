package com.hirevo.messaging;

/** Central registry of Kafka topic names — one place to grep, versioned in the name. */
public final class KafkaTopics {

  private KafkaTopics() {}

  // ---- Cross-cutting ----
  public static final String AUDIT_V1 = "hirevo.audit.v1";
  public static final String NOTIFICATION_OUT_V1 = "hirevo.notification.out.v1";

  // ---- IAM ----
  public static final String IAM_USER_EVENTS_V1 = "hirevo.iam.user.v1";

  // ---- Employee ----
  public static final String EMPLOYEE_EVENTS_V1 = "hirevo.employee.v1";

  // ---- Attendance ----
  public static final String ATTENDANCE_EVENTS_V1 = "hirevo.attendance.v1";
  public static final String ATTENDANCE_FRAUD_ALERT_V1 = "hirevo.attendance.fraud.v1";

  // ---- Payroll (saga) ----
  public static final String PAYROLL_RUN_REQUESTED_V1 = "hirevo.payroll.run.requested.v1";
  public static final String PAYROLL_RUN_CALCULATED_V1 = "hirevo.payroll.run.calculated.v1";
  public static final String PAYROLL_RUN_APPROVED_V1 = "hirevo.payroll.run.approved.v1";
  public static final String PAYSLIP_GENERATED_V1 = "hirevo.payroll.payslip.generated.v1";

  // ---- Reimbursement fraud ----
  public static final String REIMBURSEMENT_SUBMITTED_V1 = "hirevo.reimbursement.submitted.v1";
  public static final String REIMBURSEMENT_SCORED_V1 = "hirevo.reimbursement.scored.v1";

  // ---- Recruitment ----
  public static final String RECRUITMENT_APPLICATION_V1 = "hirevo.recruitment.application.v1";
}
