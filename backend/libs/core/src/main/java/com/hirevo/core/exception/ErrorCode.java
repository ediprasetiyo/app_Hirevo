package com.hirevo.core.exception;

/**
 * Standard error codes used across Hirevo services.
 * Format: {module}.{condition} — machine-readable.
 */
public enum ErrorCode {
  // Generic
  VALIDATION_FAILED("common.validation_failed", 400),
  RESOURCE_NOT_FOUND("common.not_found", 404),
  UNAUTHORIZED("common.unauthorized", 401),
  FORBIDDEN("common.forbidden", 403),
  CONFLICT("common.conflict", 409),
  BUSINESS_RULE_VIOLATION("common.business_rule_violation", 422),
  RATE_LIMITED("common.rate_limited", 429),
  INTERNAL_ERROR("common.internal_error", 500),

  // Auth / IAM
  INVALID_CREDENTIALS("auth.invalid_credentials", 401),
  MFA_REQUIRED("auth.mfa_required", 202),
  MFA_INVALID_CODE("auth.mfa_invalid_code", 401),
  MFA_CHALLENGE_EXPIRED("auth.mfa_challenge_expired", 401),
  ACCOUNT_LOCKED("auth.account_locked", 423),
  TOKEN_EXPIRED("auth.token_expired", 401),
  TOKEN_INVALID("auth.token_invalid", 401),
  REFRESH_TOKEN_REUSED("auth.refresh_token_reused", 401),

  // Tenant
  TENANT_NOT_FOUND("tenant.not_found", 404),
  TENANT_SUSPENDED("tenant.suspended", 403),
  TENANT_TRIAL_EXPIRED("tenant.trial_expired", 402),
  TENANT_SUBDOMAIN_TAKEN("tenant.subdomain_taken", 409),

  // Payroll
  PAYROLL_PERIOD_LOCKED("payroll.period_locked", 422),
  PAYROLL_FOUR_EYES_VIOLATION("payroll.four_eyes_violation", 422),

  // Attendance
  ATTENDANCE_OUT_OF_GEOFENCE("attendance.out_of_geofence", 422),
  ATTENDANCE_FACE_MISMATCH("attendance.face_mismatch", 422),
  ATTENDANCE_LIVENESS_FAILED("attendance.liveness_failed", 422),
  ATTENDANCE_MOCK_LOCATION("attendance.mock_location_detected", 422),
  ATTENDANCE_ALREADY_CLOCKED("attendance.already_clocked_in", 409);

  private final String code;
  private final int httpStatus;

  ErrorCode(String code, int httpStatus) {
    this.code = code;
    this.httpStatus = httpStatus;
  }

  public String code() {
    return code;
  }

  public int httpStatus() {
    return httpStatus;
  }
}
