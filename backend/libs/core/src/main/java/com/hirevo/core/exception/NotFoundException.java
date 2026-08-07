package com.hirevo.core.exception;

public class NotFoundException extends HirevoException {
  public NotFoundException(String resource, Object id) {
    super(ErrorCode.RESOURCE_NOT_FOUND, resource + " not found: " + id);
    withContext("resource", resource);
    withContext("id", id);
  }
}
