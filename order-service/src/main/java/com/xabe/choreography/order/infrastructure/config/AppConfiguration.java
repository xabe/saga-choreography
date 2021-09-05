package com.xabe.choreography.order.infrastructure.config;

import com.xabe.choreography.common.infrastructure.dispatch.CommandDispatcher;
import com.xabe.choreography.common.infrastructure.dispatch.CommandDispatcherImpl;
import com.xabe.choreography.common.infrastructure.event.EventPublisher;
import com.xabe.choreography.order.domain.command.PaymentCreateCommandContext;
import com.xabe.choreography.order.domain.entity.OrderAggregate;
import com.xabe.choreography.order.domain.repository.OrderRepository;
import io.quarkus.arc.DefaultBean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@ApplicationScoped
public class AppConfiguration {

  @Produces
  @DefaultBean
  public PaymentCreateCommandContext orderCreateCommandContext(final OrderRepository orderRepository,
      final @Named("PaymentEventPublisher") EventPublisher eventPublisher) {
    return new PaymentCreateCommandContext(orderRepository, eventPublisher);
  }

  @Produces
  @DefaultBean
  public CommandDispatcher<PaymentCreateCommandContext, OrderAggregate, String> orderCommandDispatcher(
      final PaymentCreateCommandContext context) {
    return new CommandDispatcherImpl<>(context);
  }

}
