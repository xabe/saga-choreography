package com.xabe.choreography.order.domain.command;

import com.xabe.choreography.common.infrastructure.dispatch.CommandContext;
import com.xabe.choreography.common.infrastructure.event.EventPublisher;
import com.xabe.choreography.common.infrastructure.repository.Repository;
import com.xabe.choreography.order.domain.entity.OrderAggregate;

public class PaymentCreateCommandContext extends CommandContext<OrderAggregate, String> {

  public PaymentCreateCommandContext(
      final Repository<OrderAggregate, String> repository,
      final EventPublisher eventPublisher) {
    super(repository, eventPublisher);
  }
}
