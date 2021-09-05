package com.xabe.orchestration.shipping.infrastructure.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.xabe.avro.v1.MessageEnvelopeStatus;
import com.xabe.avro.v1.Shipping;
import com.xabe.orchestation.common.infrastructure.Event;
import com.xabe.orchestation.common.infrastructure.event.EventPublisher;
import com.xabe.orchestration.shipping.domain.event.ShippingCreatedEvent;
import com.xabe.orchestration.shipping.infrastructure.ShippingMother;
import com.xabe.orchestration.shipping.infrastructure.messaging.mapper.MessagingMapperImpl;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

class ShippingEventPublisherTest {

  private Logger logger;

  private Emitter<MessageEnvelopeStatus> emitter;

  private EventPublisher eventPublisher;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.emitter = mock(Emitter.class);
    this.eventPublisher = new ShippingEventPublisher(this.logger, new MessagingMapperImpl(), this.emitter);
  }

  @Test
  public void givenAEventNotValidWhenInvokeTryPublishThenIgnoreEvent() throws Exception {
    //Given
    final Event event = new Event() {
    };

    //When
    this.eventPublisher.tryPublish(event);

    //Then
    verify(this.logger).warn(anyString(), eq(event));
  }

  @Test
  public void givenAEventCreatedValidWhenInvokeTryPublishThenSendEvent() throws Exception {
    //Given
    final Event event = ShippingMother.createShippingCreatedEvent();
    final ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);

    //When
    this.eventPublisher.tryPublish(event);

    //Then
    verify(this.emitter).send(messageArgumentCaptor.capture());
    verify(this.logger).info(anyString(), any(MessageEnvelopeStatus.class));

    final Message<MessageEnvelopeStatus> result = messageArgumentCaptor.getValue();
    assertThat(result, is(notNullValue()));
    this.assertMetadata(result.getMetadata());
    this.assertMessageEnvelopeStatus(result.getPayload(), event, "SUCCESS");
  }

  private void assertMetadata(final Metadata metadata) {
    assertThat(metadata, is(notNullValue()));
    assertThat(metadata.get(OutgoingKafkaRecordMetadata.class).isPresent(), is(true));
    assertThat(((OutgoingKafkaRecordMetadata) metadata.get(OutgoingKafkaRecordMetadata.class).get()).getKey(), is("1"));
  }

  private void assertMessageEnvelopeStatus(final MessageEnvelopeStatus messageEnvelopeStatus, final Event event,
      final String operationStatus) {
    assertThat(messageEnvelopeStatus, is(notNullValue()));
    this.assertMetadata(messageEnvelopeStatus.getMetadata());
    this.assertCreatedPayload(messageEnvelopeStatus.getPayload(), event, operationStatus);
  }

  private void assertCreatedPayload(final Object payload, final Event event, final String operationStatus) {
    final com.xabe.avro.v1.ShippingCreatedEvent shippingCreatedEventAvro = com.xabe.avro.v1.ShippingCreatedEvent.class.cast(payload);
    final ShippingCreatedEvent shippingCreatedEvent =
        ShippingCreatedEvent.class.cast(event);
    assertThat(shippingCreatedEventAvro, is(notNullValue()));
    assertThat(shippingCreatedEventAvro.getUpdatedAt(), is(notNullValue()));
    assertThat(shippingCreatedEventAvro.getOperationStatus().name(), is(operationStatus));
    final Shipping shipping = shippingCreatedEventAvro.getShipping();
    assertThat(shipping, is(notNullValue()));
    assertThat(shipping.getId(), is(shippingCreatedEvent.getId()));
    assertThat(shipping.getPurchaseId(), is(shippingCreatedEvent.getPurchaseId()));
    assertThat(shipping.getUserId(), is(shippingCreatedEvent.getUserId()));
    assertThat(shipping.getProductId(), is(shippingCreatedEvent.getProductId()));
    assertThat(shipping.getPrice(), is(shippingCreatedEvent.getPrice()));
    assertThat(shipping.getStatus().name(), is(shippingCreatedEvent.getStatus()));
    assertThat(shipping.getCreatedAt(), is(shippingCreatedEvent.getCreatedAt()));
  }

  private void assertMetadata(final com.xabe.avro.v1.Metadata metadata) {
    assertThat(metadata.getDomain(), is("shipping"));
    assertThat(metadata.getName(), is("shipping"));
    assertThat(metadata.getAction(), is("create"));
    assertThat(metadata.getVersion(), is("test"));
    assertThat(metadata.getTimestamp(), is((notNullValue())));
  }

}