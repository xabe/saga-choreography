package com.xabe.orchestration.order.domain.command;

import com.xabe.orchestation.common.infrastructure.dispatch.CommandContext;
import com.xabe.orchestation.common.infrastructure.event.EventPublisher;
import com.xabe.orchestation.common.infrastructure.repository.Repository;
import com.xabe.orchestration.order.domain.entity.OrderAggregate;

public class PaymentCreateCommandContext extends CommandContext<OrderAggregate, String> {

  public PaymentCreateCommandContext(
      final Repository<OrderAggregate, String> repository,
      final EventPublisher eventPublisher) {
    super(repository, eventPublisher);
  }
}
