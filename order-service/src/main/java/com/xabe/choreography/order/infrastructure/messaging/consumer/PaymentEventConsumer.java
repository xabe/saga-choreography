package com.xabe.choreography.order.infrastructure.messaging.consumer;

import com.xabe.choreography.common.infrastructure.Event;
import com.xabe.choreography.common.infrastructure.event.EventConsumer;
import com.xabe.choreography.common.infrastructure.exception.EntityNotFoundException;
import com.xabe.choreography.order.domain.entity.OrderAggregate;
import com.xabe.choreography.order.domain.entity.OrderAggregateStatus;
import com.xabe.choreography.order.domain.entity.payment.Payment;
import com.xabe.choreography.order.domain.event.payment.PaymentCreatedEvent;
import com.xabe.choreography.order.domain.repository.OrderRepository;
import com.xabe.choreography.order.infrastructure.messaging.consumer.mapper.MessagingConsumerMapper;
import io.smallrye.mutiny.Uni;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;

@ApplicationScoped
@Named("PaymentEventConsumer")
public class PaymentEventConsumer implements EventConsumer {

  public static final String SUCCESS = "SUCCESS";

  private final Logger logger;

  private final OrderRepository orderRepository;

  private final MessagingConsumerMapper messagingConsumerMapper;

  private final Map<Class, Consumer<Event>> mapHandlerEvent;

  @Inject
  public PaymentEventConsumer(
      final Logger logger,
      final MessagingConsumerMapper messagingConsumerMapper,
      final OrderRepository orderRepository) {
    this.logger = logger;
    this.messagingConsumerMapper = messagingConsumerMapper;
    this.orderRepository = orderRepository;
    this.mapHandlerEvent =
        Map.of(PaymentCreatedEvent.class, this::paymentCreatedEvent);
  }

  @Override
  public void consume(final Event event) {
    this.mapHandlerEvent.getOrDefault(event.getClass(), this::ignoreEvent).accept(event);
  }

  private void ignoreEvent(final Event event) {
    this.logger.warn("Ignore event {}", event);
  }

  private void paymentCreatedEvent(final Event event) {
    final PaymentCreatedEvent paymentCreatedEvent = PaymentCreatedEvent.class.cast(event);
    if (SUCCESS.equalsIgnoreCase(paymentCreatedEvent.getOperationStatus())) {
      final String purchaseId = paymentCreatedEvent.getPurchaseId();
      final Payment payment = this.messagingConsumerMapper.toPaymentEntity(paymentCreatedEvent);
      this.orderRepository.load(purchaseId)
          .onItem().ifNull().failWith(() -> new EntityNotFoundException("OrderAggregate"))
          .flatMap(this.updateOrderAggregate(payment))
          .subscribe().with(this::successUpdateOrderAggregate, this.errorUpdateOrderAggregate(purchaseId));
    } else {
      this.logger.error("Error to create shipping {}", paymentCreatedEvent);
    }
  }

  private Consumer<Throwable> errorUpdateOrderAggregate(final String id) {
    return throwable -> this.logger.error("Error to save orderAggregate {} with payment create command", id, throwable);
  }

  private void successUpdateOrderAggregate(final OrderAggregate orderAggregate) {
    this.logger.info("Update info payment success for {}", orderAggregate.getId());
  }

  private Function<OrderAggregate, Uni<? extends OrderAggregate>> updateOrderAggregate(final Payment payment) {
    return orderAggregate -> {
      orderAggregate.setPayment(payment);
      orderAggregate.setStatus(OrderAggregateStatus.SHIPPING_SENT);
      return this.orderRepository.update(orderAggregate);
    };
  }
}

