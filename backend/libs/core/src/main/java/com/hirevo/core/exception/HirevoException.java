package com.hirevo.core.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Base runtime exception carrying a machine-readable {@link ErrorCode}
 * plus optional context fields surfaced in the RFC 7807 problem+json response.
 */
public class HirevoException extends RuntimeException {

  private final ErrorCode errorCode;
  private final Map<String, Object> context = new HashMap<>();

  public HirevoException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public HirevoException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public HirevoException withContext(String key, Object value) {
    context.put(key, value);
    return this;
  }

  public ErrorCode errorCode() {
    return errorCode;
  }

  public Map<String, Object> context() {
    return context;
  }
}
