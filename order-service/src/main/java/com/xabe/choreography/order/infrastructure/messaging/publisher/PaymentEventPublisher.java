package com.xabe.choreography.order.infrastructure.messaging.publisher;

import com.xabe.avro.v1.MessageEnvelopePayment;
import com.xabe.avro.v1.Metadata;
import com.xabe.avro.v1.PaymentCreateCommand;
import com.xabe.choreography.common.infrastructure.Event;
import com.xabe.choreography.common.infrastructure.event.EventPublisher;
import com.xabe.choreography.order.domain.event.payment.PaymentCreateCommandEvent;
import com.xabe.choreography.order.infrastructure.messaging.publisher.mapper.MessagingPublisherMapper;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Consumer;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;

@ApplicationScoped
@Named("PaymentEventPublisher")
public class PaymentEventPublisher implements EventPublisher {

  public static final String PAYMENT = "payment";

  public static final String TEST = "test";

  public static final String CREATE = "create";

  private final Logger logger;

  private final MessagingPublisherMapper messagingPublisherMapper;

  private final Emitter<MessageEnvelopePayment> paymentEmitter;

  private final Map<Class, Consumer<Event>> mapHandlerEvent;

  @Inject
  public PaymentEventPublisher(final Logger logger, final MessagingPublisherMapper messagingPublisherMapper,
      @Channel("payments") final Emitter<MessageEnvelopePayment> paymentEmitter) {
    this.logger = logger;
    this.messagingPublisherMapper = messagingPublisherMapper;
    this.paymentEmitter = paymentEmitter;
    this.mapHandlerEvent = Map.of(PaymentCreateCommandEvent.class, this::paymentCreateCommand);
  }

  @Override
  public void tryPublish(final Event event) {
    this.mapHandlerEvent.getOrDefault(event.getClass(), this::ignoreEvent).accept(event);
  }

  private void ignoreEvent(final Event event) {
    this.logger.warn("Ignore event {}", event);
  }

  private void paymentCreateCommand(final Event event) {
    final PaymentCreateCommandEvent paymentCreateCommandEvent = PaymentCreateCommandEvent.class.cast(event);
    final PaymentCreateCommand paymentCreateCommand = this.messagingPublisherMapper.toAvroCommandEvent(paymentCreateCommandEvent);
    final MessageEnvelopePayment messageEnvelopePayment =
        MessageEnvelopePayment.newBuilder().setMetadata(this.createMetaData()).setPayload(paymentCreateCommand).build();
    this.paymentEmitter.send(Message.of(messageEnvelopePayment, this.createMetaDataKafka(paymentCreateCommandEvent.getPurchaseId())));
    this.logger.info("Send Event PaymentCreatedEvent {}", messageEnvelopePayment);
  }

  private Metadata createMetaData() {
    return Metadata.newBuilder().setDomain(PAYMENT).setName(PAYMENT).setAction(CREATE).setVersion(TEST)
        .setTimestamp(DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())).build();
  }

  private org.eclipse.microprofile.reactive.messaging.Metadata createMetaDataKafka(final String key) {
    return org.eclipse.microprofile.reactive.messaging.Metadata.of(OutgoingKafkaRecordMetadata.builder().withKey(key).build());
  }
}
