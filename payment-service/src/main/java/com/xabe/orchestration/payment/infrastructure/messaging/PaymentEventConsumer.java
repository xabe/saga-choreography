package com.xabe.orchestration.payment.infrastructure.messaging;

import com.xabe.orchestation.common.infrastructure.Event;
import com.xabe.orchestation.common.infrastructure.event.EventConsumer;
import com.xabe.orchestation.common.infrastructure.event.EventPublisher;
import com.xabe.orchestration.payment.domain.entity.Payment;
import com.xabe.orchestration.payment.domain.event.PaymentCreateCommandEvent;
import com.xabe.orchestration.payment.domain.repository.PaymentRepository;
import com.xabe.orchestration.payment.infrastructure.messaging.mapper.MessagingMapper;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.slf4j.Logger;

@ApplicationScoped
public class PaymentEventConsumer implements EventConsumer {

  public static final String PAYMENT_CREATE_COMMAND_EVENT = "PaymentCreateCommandEvent";

  public static final String ERROR = "ERROR";

  public static final String SUCCESS = "SUCCESS";

  private final Logger logger;

  private final PaymentRepository paymentRepository;

  private final MessagingMapper messagingMapper;

  private final EventPublisher eventPublisher;

  private final Map<Class, Consumer<Event>> mapHandlerEvent;

  @Inject
  public PaymentEventConsumer(final Logger logger, final PaymentRepository paymentRepository, final MessagingMapper messagingMapper,
      final EventPublisher eventPublisher) {
    this.logger = logger;
    this.paymentRepository = paymentRepository;
    this.messagingMapper = messagingMapper;
    this.eventPublisher = eventPublisher;
    this.mapHandlerEvent =
        Map.of(PaymentCreateCommandEvent.class, this::paymentCreateCommandEvent);
  }

  @Override
  public void consume(final Event event) {
    this.mapHandlerEvent.getOrDefault(event.getClass(), this::ignoreEvent).accept(event);
  }

  private void ignoreEvent(final Event event) {
    this.logger.warn("Ignore event {}", event);
  }

  private void paymentCreateCommandEvent(final Event event) {
    final PaymentCreateCommandEvent paymentCreateCommandEvent = PaymentCreateCommandEvent.class.cast(event);
    final Payment payment = this.messagingMapper.toEntity(paymentCreateCommandEvent);
    this.paymentRepository.create(payment).subscribe().with(
        this.sendEventSuccess(this.messagingMapper::toCreatedEvent),
        this.sendEventError(PAYMENT_CREATE_COMMAND_EVENT, payment, this.messagingMapper::toCreatedEvent));
  }

  private Consumer<Throwable> sendEventError(final String command, final Payment payment,
      final BiFunction<Payment, String, Event> mapping) {
    return throwable -> {
      this.eventPublisher.tryPublish(mapping.apply(payment, ERROR));
      this.logger.error("Error to save command {} with payment: {}", command, payment, throwable);
    };
  }

  private Consumer<Payment> sendEventSuccess(final BiFunction<Payment, String, Event> mapping) {
    return payment -> {
      this.eventPublisher.tryPublish(mapping.apply(payment, SUCCESS));
    };
  }
}
