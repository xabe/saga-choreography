package com.xabe.orchestration.payment.infrastructure.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.xabe.avro.v1.MessageEnvelopeShipping;
import com.xabe.avro.v1.MessageEnvelopeStatus;
import com.xabe.avro.v1.Payment;
import com.xabe.avro.v1.PaymentCreatedEvent;
import com.xabe.avro.v1.ShippingCreateCommand;
import com.xabe.orchestation.common.infrastructure.Event;
import com.xabe.orchestation.common.infrastructure.event.EventPublisher;
import com.xabe.orchestration.payment.infrastructure.PaymentMother;
import com.xabe.orchestration.payment.infrastructure.messaging.mapper.MessagingMapperImpl;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

class PaymentEventPublisherTest {

  private Logger logger;

  private Emitter<MessageEnvelopeStatus> statusEmitter;

  private Emitter<MessageEnvelopeShipping> shippingEmitter;

  private EventPublisher eventPublisher;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.statusEmitter = mock(Emitter.class);
    this.shippingEmitter = mock(Emitter.class);
    this.eventPublisher = new PaymentEventPublisher(this.logger, new MessagingMapperImpl(), this.statusEmitter, this.shippingEmitter);
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
    final com.xabe.orchestration.payment.domain.event.PaymentCreatedEvent event = PaymentMother.createPaymentCreatedEvent();
    final ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);

    //When
    this.eventPublisher.tryPublish(event);

    //Then
    verify(this.statusEmitter).send(messageArgumentCaptor.capture());
    verify(this.shippingEmitter).send(messageArgumentCaptor.capture());
    verify(this.logger).info(anyString(), any(MessageEnvelopeStatus.class));

    final Message<MessageEnvelopeStatus> statusMessage = messageArgumentCaptor.getAllValues().get(0);
    assertThat(statusMessage, is(notNullValue()));
    this.assertMetadata(statusMessage.getMetadata());
    this.assertMessageEnvelopeStatus(statusMessage.getPayload(), event, "SUCCESS");
    final Message<MessageEnvelopeShipping> shippingMessage = messageArgumentCaptor.getAllValues().get(1);
    final ShippingCreateCommand shippingCreateCommand = ShippingCreateCommand.class.cast(shippingMessage.getPayload().getPayload());
    assertThat(shippingCreateCommand, is(notNullValue()));
    assertThat(shippingCreateCommand.getPurchaseId(), is(event.getPurchaseId()));
    assertThat(shippingCreateCommand.getPrice(), is(event.getPrice()));
    assertThat(shippingCreateCommand.getProductId(), is(event.getProductId()));
    assertThat(shippingCreateCommand.getUserId(), is(event.getUserId()));
    assertThat(shippingCreateCommand.getSentAt(), is(notNullValue()));
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
    final PaymentCreatedEvent paymentCreatedEventAvro = PaymentCreatedEvent.class.cast(payload);
    final com.xabe.orchestration.payment.domain.event.PaymentCreatedEvent paymentCreatedEvent =
        com.xabe.orchestration.payment.domain.event.PaymentCreatedEvent.class.cast(event);
    assertThat(paymentCreatedEventAvro, is(notNullValue()));
    assertThat(paymentCreatedEventAvro.getUpdatedAt(), is(notNullValue()));
    assertThat(paymentCreatedEventAvro.getOperationStatus().name(), is(operationStatus));
    final Payment payment = paymentCreatedEventAvro.getPayment();
    assertThat(payment, is(notNullValue()));
    assertThat(payment.getId(), is(paymentCreatedEvent.getId()));
    assertThat(payment.getPurchaseId(), is(paymentCreatedEvent.getPurchaseId()));
    assertThat(payment.getUserId(), is(paymentCreatedEvent.getUserId()));
    assertThat(payment.getProductId(), is(paymentCreatedEvent.getProductId()));
    assertThat(payment.getPrice(), is(paymentCreatedEvent.getPrice()));
    assertThat(payment.getStatus().name(), is(paymentCreatedEvent.getStatus()));
    assertThat(payment.getCreatedAt(), is(paymentCreatedEvent.getCreatedAt()));
  }

  private void assertMetadata(final com.xabe.avro.v1.Metadata metadata) {
    assertThat(metadata.getDomain(), is("payment"));
    assertThat(metadata.getName(), is("payment"));
    assertThat(metadata.getAction(), is("create"));
    assertThat(metadata.getVersion(), is("test"));
    assertThat(metadata.getTimestamp(), is((notNullValue())));
  }

}