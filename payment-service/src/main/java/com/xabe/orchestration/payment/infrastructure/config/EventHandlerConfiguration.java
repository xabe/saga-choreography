package com.xabe.orchestration.payment.infrastructure.config;

import com.xabe.avro.v1.PaymentCreateCommand;
import com.xabe.orchestation.common.infrastructure.event.EventHandler;
import com.xabe.orchestation.common.infrastructure.event.SimpleEventHandler;
import com.xabe.orchestration.payment.infrastructure.messaging.PaymentEventConsumer;
import com.xabe.orchestration.payment.infrastructure.messaging.mapper.MessagingMapper;
import io.quarkus.arc.DefaultBean;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@ApplicationScoped
public class EventHandlerConfiguration {

  @Named("paymentHandlers")
  @Produces
  @DefaultBean
  public Map<Class, EventHandler> paymentEventHandlers(final PaymentEventConsumer paymentEventConsumer,
      final MessagingMapper messagingMapper) {
    final Map<Class, EventHandler> eventHandlers = new HashMap<>();
    eventHandlers.put(PaymentCreateCommand.class,
        new SimpleEventHandler<>(messagingMapper::toAvroCreateCommandEvent, paymentEventConsumer::consume));
    return eventHandlers;
  }

}
