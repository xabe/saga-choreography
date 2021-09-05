package com.xabe.choreography.order.infrastructure.application;

import com.xabe.choreography.common.infrastructure.dispatch.CommandDispatcher;
import com.xabe.choreography.order.domain.command.PaymentCreateCommand;
import com.xabe.choreography.order.domain.command.PaymentCreateCommandContext;
import com.xabe.choreography.order.domain.entity.OrderAggregate;
import com.xabe.choreography.order.domain.repository.OrderRepository;
import io.smallrye.mutiny.Uni;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class OrderUseCaseImpl implements OrderUseCase {

  private final OrderRepository orderRepository;

  private final CommandDispatcher<PaymentCreateCommandContext, OrderAggregate, String> commandDispatcher;

  @Override
  public Uni<List<OrderAggregate>> getOrders() {
    return this.orderRepository.getAll();
  }

  @Override
  public Uni<OrderAggregate> getOrder(final String id) {
    return this.orderRepository.load(id);
  }

  @Override
  public Uni<OrderAggregate> create(final OrderAggregate orderAggregate) {
    return this.orderRepository.save(orderAggregate).flatMap(this::sendPaymentCreateCommand);
  }

  private Uni<? extends OrderAggregate> sendPaymentCreateCommand(final OrderAggregate orderAggregate) {
    return this.commandDispatcher.dispatch(new PaymentCreateCommand(orderAggregate.getId(), orderAggregate));
  }

}
