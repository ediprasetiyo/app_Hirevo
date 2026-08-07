package com.hirevo.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Central exception → RFC 7807 Problem+JSON translator.
 * All Hirevo services import this via {@code hirevo-core} + component scan.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(HirevoException.class)
  public ResponseEntity<ProblemDetail> handleHirevo(HirevoException ex, HttpServletRequest req) {
    ProblemDetail pd = problem(ex.errorCode(), ex.getMessage(), req);
    ex.context().forEach(pd::setProperty);
    return ResponseEntity.status(ex.errorCode().httpStatus()).body(pd);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest req) {
    ProblemDetail pd = problem(ErrorCode.VALIDATION_FAILED, "Validation failed", req);
    List<Map<String, String>> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(GlobalExceptionHandler::fieldErrorMap)
            .toList();
    pd.setProperty("errors", fieldErrors);
    return ResponseEntity.badRequest().body(pd);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ProblemDetail> handleAuth(AuthenticationException ex, HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(problem(ErrorCode.UNAUTHORIZED, ex.getMessage(), req));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> handleForbidden(AccessDeniedException ex, HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(problem(ErrorCode.FORBIDDEN, ex.getMessage(), req));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest req) {
    log.error("Unhandled error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(problem(ErrorCode.INTERNAL_ERROR, "Unexpected server error", req));
  }

  private static ProblemDetail problem(ErrorCode code, String detail, HttpServletRequest req) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
        HttpStatus.valueOf(code.httpStatus() == 202 ? 401 : code.httpStatus()), detail);
    pd.setType(URI.create("https://api.hirevo.id/errors/" + code.code()));
    pd.setTitle(code.code());
    pd.setInstance(URI.create(req.getRequestURI()));
    pd.setProperty("code", code.code());
    return pd;
  }

  private static Map<String, String> fieldErrorMap(FieldError fe) {
    return Map.of(
        "field", fe.getField(),
        "code", fe.getCode() == null ? "invalid" : fe.getCode(),
        "message", fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage());
  }
}
