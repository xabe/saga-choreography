package com.xabe.orchestration.order.infrastructure.messaging.consumer;

import com.xabe.orchestation.common.infrastructure.Event;
import com.xabe.orchestation.common.infrastructure.event.EventConsumer;
import com.xabe.orchestation.common.infrastructure.exception.EntityNotFoundException;
import com.xabe.orchestration.order.domain.entity.OrderAggregate;
import com.xabe.orchestration.order.domain.entity.OrderAggregateStatus;
import com.xabe.orchestration.order.domain.entity.shipping.Shipping;
import com.xabe.orchestration.order.domain.event.shipping.ShippingCreatedEvent;
import com.xabe.orchestration.order.domain.repository.OrderRepository;
import com.xabe.orchestration.order.infrastructure.messaging.consumer.mapper.MessagingConsumerMapper;
import io.smallrye.mutiny.Uni;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;

@ApplicationScoped
@Named("ShippingEventConsumer")
public class ShippingEventConsumer implements EventConsumer {

  public static final String SUCCESS = "SUCCESS";

  private final Logger logger;

  private final OrderRepository orderRepository;

  private final MessagingConsumerMapper messagingConsumerMapper;

  private final Map<Class, Consumer<Event>> mapHandlerEvent;

  @Inject
  public ShippingEventConsumer(
      final Logger logger,
      final MessagingConsumerMapper messagingConsumerMapper,
      final OrderRepository orderRepository) {
    this.logger = logger;
    this.messagingConsumerMapper = messagingConsumerMapper;
    this.orderRepository = orderRepository;
    this.mapHandlerEvent = Map.of(ShippingCreatedEvent.class, this::shippingCreatedEvent);
  }

  @Override
  public void consume(final Event event) {
    this.mapHandlerEvent.getOrDefault(event.getClass(), this::ignoreEvent).accept(event);
  }

  private void ignoreEvent(final Event event) {
    this.logger.warn("Ignore event {}", event);
  }

  private void shippingCreatedEvent(final Event event) {
    final ShippingCreatedEvent shippingCreatedEvent = ShippingCreatedEvent.class.cast(event);
    if (SUCCESS.equalsIgnoreCase(shippingCreatedEvent.getOperationStatus())) {
      final Shipping shipping = this.messagingConsumerMapper.toShippingEntity(shippingCreatedEvent);
      final String purchaseId = shippingCreatedEvent.getPurchaseId();
      this.orderRepository.load(purchaseId)
          .onItem().ifNull().failWith(() -> new EntityNotFoundException("OrderAggregate"))
          .flatMap(this.updateOrderAggregate(shipping))
          .subscribe().with(this::successUpdateOrderAggregate, this.errorUpdateOrderAggregate(purchaseId));

    } else {
      this.logger.error("Error to create shipping {}", shippingCreatedEvent);
    }
  }

  private Consumer<Throwable> errorUpdateOrderAggregate(final String id) {
    return throwable -> this.logger.error("Error to save orderAggregate {} with payment create command", id, throwable);
  }

  private void successUpdateOrderAggregate(final OrderAggregate orderAggregate) {
    final OffsetDateTime now = OffsetDateTime.now();
    this.logger.info("End Saga order {} time {} duration {} seconds", orderAggregate.getId(),
        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now), Duration.between(orderAggregate.getCreatedAt(), now).getSeconds());
  }

  private Function<OrderAggregate, Uni<? extends OrderAggregate>> updateOrderAggregate(final Shipping shipping) {
    return orderAggregate -> {
      orderAggregate.setShipping(shipping);
      orderAggregate.setStatus(OrderAggregateStatus.SUCCESS);
      return this.orderRepository.update(orderAggregate);
    };
  }

}
