package com.xabe.orchestation.common.infrastructure.exception;

public class EntityNotFoundException extends RuntimeException {

  public EntityNotFoundException(final String entity) {
    super("Not found entity " + entity);
  }
}
