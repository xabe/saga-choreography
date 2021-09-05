package com.xabe.choreography.payment.infrastructure.messaging;

import com.xabe.avro.v1.MessageEnvelopeShipping;
import com.xabe.avro.v1.MessageEnvelopeStatus;
import com.xabe.avro.v1.Metadata;
import com.xabe.avro.v1.Payment;
import com.xabe.avro.v1.PaymentOperationStatus;
import com.xabe.avro.v1.ShippingCreateCommand;
import com.xabe.choreography.common.infrastructure.Event;
import com.xabe.choreography.common.infrastructure.event.EventPublisher;
import com.xabe.choreography.payment.domain.event.PaymentCreatedEvent;
import com.xabe.choreography.payment.infrastructure.messaging.mapper.MessagingMapper;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Consumer;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import jdk.jfr.Name;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;

@ApplicationScoped
@Name("PaymentEventPublisher")
public class PaymentEventPublisher implements EventPublisher {

  public static final String PAYMENT = "payment";

  public static final String TEST = "test";

  public static final String CREATE = "create";

  private final Logger logger;

  private final MessagingMapper messagingMapper;

  private final Emitter<MessageEnvelopeStatus> statusEmitter;

  final Emitter<MessageEnvelopeShipping> shipmentsEmitter;

  private final Map<Class, Consumer<Event>> mapHandlerEvent;

  @Inject
  public PaymentEventPublisher(final Logger logger, final MessagingMapper messagingMapper,
      @Channel("status") final Emitter<MessageEnvelopeStatus> statusEmitter,
      @Channel("shipments") final Emitter<MessageEnvelopeShipping> shipmentsEmitter) {
    this.logger = logger;
    this.messagingMapper = messagingMapper;
    this.statusEmitter = statusEmitter;
    this.shipmentsEmitter = shipmentsEmitter;
    this.mapHandlerEvent =
        Map.of(PaymentCreatedEvent.class, this::paymentCreatedEvent);
  }

  @Override
  public void tryPublish(final Event event) {
    this.mapHandlerEvent.getOrDefault(event.getClass(), this::ignoreEvent).accept(event);
  }

  private void ignoreEvent(final Event event) {
    this.logger.warn("Ignore event {}", event);
  }

  private void paymentCreatedEvent(final Event event) {
    final PaymentCreatedEvent paymentCreatedEvent = PaymentCreatedEvent.class.cast(event);
    final Payment payment = this.messagingMapper.toAvroEvent(paymentCreatedEvent);
    final com.xabe.avro.v1.PaymentCreatedEvent createdEvent = com.xabe.avro.v1.PaymentCreatedEvent.newBuilder()
        .setPayment(payment)
        .setOperationStatus(PaymentOperationStatus.valueOf(paymentCreatedEvent.getOperationStatus()))
        .setUpdatedAt(Instant.now())
        .build();
    final MessageEnvelopeStatus messageEnvelopeStatus =
        MessageEnvelopeStatus.newBuilder().setMetadata(this.createMetaData()).setPayload(createdEvent).build();
    this.statusEmitter.send(Message.of(messageEnvelopeStatus, this.createMetaDataKafka(paymentCreatedEvent.getId().toString())));
    final ShippingCreateCommand shippingCreateCommand = ShippingCreateCommand.newBuilder()
        .setPurchaseId(paymentCreatedEvent.getPurchaseId())
        .setProductId(paymentCreatedEvent.getProductId())
        .setUserId(paymentCreatedEvent.getUserId())
        .setPrice(paymentCreatedEvent.getPrice())
        .setSentAt(Instant.now())
        .build();
    final MessageEnvelopeShipping messageEnvelopeShipping =
        MessageEnvelopeShipping.newBuilder().setMetadata(this.createMetaData()).setPayload(shippingCreateCommand).build();
    this.shipmentsEmitter.send(Message.of(messageEnvelopeShipping, this.createMetaDataKafka(paymentCreatedEvent.getPurchaseId())));
    this.logger.info("Send Event PaymentCreatedEvent {}", messageEnvelopeStatus);
  }

  private Metadata createMetaData() {
    return Metadata.newBuilder().setDomain(PAYMENT).setName(PAYMENT).setAction(CREATE).setVersion(TEST)
        .setTimestamp(DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())).build();
  }

  private org.eclipse.microprofile.reactive.messaging.Metadata createMetaDataKafka(final String key) {
    return org.eclipse.microprofile.reactive.messaging.Metadata.of(OutgoingKafkaRecordMetadata.builder().withKey(key).build());
  }
}
