package com.xabe.orchestation.common.infrastructure.dispatch;

import com.xabe.orchestation.common.infrastructure.AggregateRoot;
import com.xabe.orchestation.common.infrastructure.event.EventPublisher;
import com.xabe.orchestation.common.infrastructure.repository.Repository;

public class CommandContext<T extends AggregateRoot<U>, U> {

  private final Repository<T, U> repository;

  private final EventPublisher eventPublisher;

  public CommandContext(final Repository<T, U> repository, final EventPublisher eventPublisher) {
    this.repository = repository;
    this.eventPublisher = eventPublisher;
  }

  public Repository<T, U> getRepository() {
    return this.repository;
  }

  public EventPublisher getEventPublisher() {
    return this.eventPublisher;
  }
}
