package com.xabe.choreography.order.infrastructure.messaging.publisher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.xabe.avro.v1.MessageEnvelopePayment;
import com.xabe.avro.v1.PaymentCreateCommand;
import com.xabe.choreography.common.infrastructure.Event;
import com.xabe.choreography.common.infrastructure.event.EventPublisher;
import com.xabe.choreography.order.domain.event.payment.PaymentCreateCommandEvent;
import com.xabe.choreography.order.infrastructure.OrderMother;
import com.xabe.choreography.order.infrastructure.messaging.publisher.mapper.MessagingPublisherMapperImpl;
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

  private Emitter<MessageEnvelopePayment> emitter;

  private EventPublisher eventPublisher;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.emitter = mock(Emitter.class);
    this.eventPublisher = new PaymentEventPublisher(this.logger, new MessagingPublisherMapperImpl(), this.emitter);
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
    final PaymentCreateCommandEvent event = OrderMother.createPaymentCreateCommandEvent();
    final ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);

    //When
    this.eventPublisher.tryPublish(event);

    //Then
    verify(this.emitter).send(messageArgumentCaptor.capture());
    verify(this.logger).info(anyString(), any(MessageEnvelopePayment.class));

    final Message<MessageEnvelopePayment> result = messageArgumentCaptor.getValue();
    assertThat(result, is(notNullValue()));
    this.assertMetadata(result.getMetadata());
    this.assertMessageEnvelopePayment(result.getPayload(), event, "SUCCESS");
  }

  private void assertMetadata(final Metadata metadata) {
    assertThat(metadata, is(notNullValue()));
    assertThat(metadata.get(OutgoingKafkaRecordMetadata.class).isPresent(), is(true));
    assertThat(((OutgoingKafkaRecordMetadata) metadata.get(OutgoingKafkaRecordMetadata.class).get()).getKey(),
        is("612cb1ac04e7df1b34068c21"));
  }

  private void assertMessageEnvelopePayment(final MessageEnvelopePayment messageEnvelopePayment, final Event event,
      final String operationStatus) {
    assertThat(messageEnvelopePayment, is(notNullValue()));
    this.assertMetadata(messageEnvelopePayment.getMetadata());
    this.assertCreatedPayload(messageEnvelopePayment.getPayload(), event);
  }

  private void assertCreatedPayload(final Object payload, final Event event) {
    final PaymentCreateCommand paymentCreateCommand = PaymentCreateCommand.class.cast(payload);
    final PaymentCreateCommandEvent paymentCreateCommandEvent = PaymentCreateCommandEvent.class.cast(event);
    assertThat(paymentCreateCommand, is(notNullValue()));
    assertThat(paymentCreateCommand.getPurchaseId(), is(paymentCreateCommandEvent.getPurchaseId()));
    assertThat(paymentCreateCommand.getUserId(), is(paymentCreateCommandEvent.getUserId()));
    assertThat(paymentCreateCommand.getProductId(), is(paymentCreateCommandEvent.getProductId()));
    assertThat(paymentCreateCommand.getPrice(), is(paymentCreateCommandEvent.getPrice()));
    assertThat(paymentCreateCommand.getSentAt(), is(paymentCreateCommandEvent.getSentAt()));
  }

  private void assertMetadata(final com.xabe.avro.v1.Metadata metadata) {
    assertThat(metadata.getDomain(), is("payment"));
    assertThat(metadata.getName(), is("payment"));
    assertThat(metadata.getAction(), is("create"));
    assertThat(metadata.getVersion(), is("test"));
    assertThat(metadata.getTimestamp(), is((notNullValue())));
  }

}