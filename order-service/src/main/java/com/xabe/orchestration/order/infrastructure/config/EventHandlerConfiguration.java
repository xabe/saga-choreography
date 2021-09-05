package com.xabe.orchestration.order.infrastructure.config;

import com.xabe.avro.v1.PaymentCreatedEvent;
import com.xabe.avro.v1.ShippingCreatedEvent;
import com.xabe.orchestation.common.infrastructure.event.EventHandler;
import com.xabe.orchestation.common.infrastructure.event.SimpleEventHandler;
import com.xabe.orchestration.order.infrastructure.messaging.consumer.PaymentEventConsumer;
import com.xabe.orchestration.order.infrastructure.messaging.consumer.ShippingEventConsumer;
import com.xabe.orchestration.order.infrastructure.messaging.consumer.mapper.MessagingConsumerMapper;
import io.quarkus.arc.DefaultBean;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@ApplicationScoped
public class EventHandlerConfiguration {

  @Named("orderHandlers")
  @Produces
  @DefaultBean
  public Map<Class, EventHandler> orderEventHandlers(final PaymentEventConsumer paymentEventConsumer,
      final ShippingEventConsumer shippingEventConsumer,
      final MessagingConsumerMapper messagingMapper) {
    final Map<Class, EventHandler> eventHandlers = new HashMap<>();
    eventHandlers.put(PaymentCreatedEvent.class,
        new SimpleEventHandler<>(messagingMapper::toAvroPaymentCreatedEvent, paymentEventConsumer::consume));
    eventHandlers.put(ShippingCreatedEvent.class,
        new SimpleEventHandler<>(messagingMapper::toAvroShippingCreatedEvent, shippingEventConsumer::consume));
    return eventHandlers;
  }

}
