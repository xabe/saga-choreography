package com.xabe.choreography.order.domain.command;

import com.xabe.choreography.common.infrastructure.Command;
import com.xabe.choreography.order.domain.entity.OrderAggregate;
import com.xabe.choreography.order.domain.entity.OrderAggregateStatus;
import com.xabe.choreography.order.domain.event.payment.PaymentCreateCommandEvent;
import io.smallrye.mutiny.Uni;
import java.time.Instant;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
@RequiredArgsConstructor
public class PaymentCreateCommand implements Command<PaymentCreateCommandContext, OrderAggregate, String> {

  Logger logger = LoggerFactory.getLogger(PaymentCreateCommand.class);

  String aggregateRootId;

  OrderAggregate orderAggregate;

  @Override
  public Uni<OrderAggregate> execute(final PaymentCreateCommandContext context) {
    return context.getRepository().update(this.orderAggregate.toBuilder().status(OrderAggregateStatus.ORDER_CREATED).build())
        .invoke(this.sendOrderCreateCommand(context));
  }

  private Consumer<OrderAggregate> sendOrderCreateCommand(final PaymentCreateCommandContext context) {
    return orderAggregate -> {
      final PaymentCreateCommandEvent paymentCreateCommandEvent = PaymentCreateCommandEvent.builder()
          .purchaseId(orderAggregate.getId())
          .price(orderAggregate.getPrice())
          .userId(orderAggregate.getUserId())
          .productId(orderAggregate.getProductId())
          .sentAt(Instant.now())
          .build();
      context.getEventPublisher().tryPublish(paymentCreateCommandEvent);
    };
  }

}
