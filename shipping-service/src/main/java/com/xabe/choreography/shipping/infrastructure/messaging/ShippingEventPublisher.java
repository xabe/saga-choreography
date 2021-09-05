package com.xabe.choreography.shipping.infrastructure.messaging;

import com.xabe.avro.v1.MessageEnvelopeStatus;
import com.xabe.avro.v1.Metadata;
import com.xabe.avro.v1.Shipping;
import com.xabe.avro.v1.ShippingOperationStatus;
import com.xabe.choreography.common.infrastructure.Event;
import com.xabe.choreography.common.infrastructure.event.EventPublisher;
import com.xabe.choreography.shipping.domain.event.ShippingCreatedEvent;
import com.xabe.choreography.shipping.infrastructure.messaging.mapper.MessagingMapper;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Consumer;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;

@ApplicationScoped
public class ShippingEventPublisher implements EventPublisher {

  public static final String SHIPPING = "shipping";

  public static final String TEST = "test";

  public static final String CREATE = "create";

  private final Logger logger;

  private final MessagingMapper messagingMapper;

  private final Emitter<MessageEnvelopeStatus> statusEmitter;

  private final Map<Class, Consumer<Event>> mapHandlerEvent;

  @Inject
  public ShippingEventPublisher(final Logger logger, final MessagingMapper messagingMapper,
      @Channel("status") final Emitter<MessageEnvelopeStatus> statusEmitter) {
    this.logger = logger;
    this.messagingMapper = messagingMapper;
    this.statusEmitter = statusEmitter;
    this.mapHandlerEvent =
        Map.of(ShippingCreatedEvent.class, this::shippingCreatedEvent);
  }

  @Override
  public void tryPublish(final Event event) {
    this.mapHandlerEvent.getOrDefault(event.getClass(), this::ignoreEvent).accept(event);
  }

  private void ignoreEvent(final Event event) {
    this.logger.warn("Ignore event {}", event);
  }

  private void shippingCreatedEvent(final Event event) {
    final ShippingCreatedEvent shippingCreatedEvent = ShippingCreatedEvent.class.cast(event);
    final Shipping shipping = this.messagingMapper.toAvroEvent(shippingCreatedEvent);
    final com.xabe.avro.v1.ShippingCreatedEvent createdEvent = com.xabe.avro.v1.ShippingCreatedEvent.newBuilder()
        .setShipping(shipping)
        .setOperationStatus(ShippingOperationStatus.valueOf(shippingCreatedEvent.getOperationStatus()))
        .setUpdatedAt(Instant.now())
        .build();
    final MessageEnvelopeStatus messageEnvelopeStatus =
        MessageEnvelopeStatus.newBuilder().setMetadata(this.createMetaData()).setPayload(createdEvent).build();
    this.statusEmitter.send(Message.of(messageEnvelopeStatus, this.createMetaDataKafka(shippingCreatedEvent.getId().toString())));
    this.logger.info("Send Event ShippingCreatedEvent {}", messageEnvelopeStatus);
  }

  private Metadata createMetaData() {
    return Metadata.newBuilder().setDomain(SHIPPING).setName(SHIPPING).setAction(CREATE).setVersion(TEST)
        .setTimestamp(DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())).build();
  }

  private org.eclipse.microprofile.reactive.messaging.Metadata createMetaDataKafka(final String key) {
    return org.eclipse.microprofile.reactive.messaging.Metadata.of(OutgoingKafkaRecordMetadata.builder().withKey(key).build());
  }
}
