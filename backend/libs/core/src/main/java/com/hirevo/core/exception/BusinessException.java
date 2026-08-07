package com.hirevo.core.exception;

/** Business-rule violation — HTTP 422 by default. */
public class BusinessException extends HirevoException {

  public BusinessException(String message) {
    super(ErrorCode.BUSINESS_RULE_VIOLATION, message);
  }

  public BusinessException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
