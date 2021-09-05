package com.xabe.choreography.payment.infrastructure.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xabe.choreography.common.infrastructure.Event;
import com.xabe.choreography.common.infrastructure.event.EventConsumer;
import com.xabe.choreography.common.infrastructure.event.EventPublisher;
import com.xabe.choreography.payment.domain.entity.Payment;
import com.xabe.choreography.payment.domain.event.PaymentCreateCommandEvent;
import com.xabe.choreography.payment.domain.event.PaymentCreatedEvent;
import com.xabe.choreography.payment.domain.repository.PaymentRepository;
import com.xabe.choreography.payment.infrastructure.PaymentMother;
import com.xabe.choreography.payment.infrastructure.messaging.mapper.MessagingMapperImpl;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

class PaymentEventConsumerTest {

  private Logger logger;

  private PaymentRepository paymentRepository;

  private EventPublisher eventPublisher;

  private EventConsumer eventConsumer;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.paymentRepository = mock(PaymentRepository.class);
    this.eventPublisher = mock(EventPublisher.class);
    this.eventConsumer = new PaymentEventConsumer(this.logger, this.paymentRepository, new MessagingMapperImpl(), this.eventPublisher);
  }

  @Test
  public void givenAEventNotValidWhenInvokeTryPublishThenIgnoreEvent() throws Exception {
    //Given
    final Event event = new Event() {
    };

    //When
    this.eventConsumer.consume(event);

    //Then
    verify(this.logger).warn(anyString(), eq(event));
    verify(this.eventPublisher, never()).tryPublish(any());
  }

  @Test
  public void givenAEventValidWhenInvokeTryPublishThenSendEvent() throws Exception {
    //Given
    final PaymentCreateCommandEvent event = PaymentMother.createPaymentCreateCommandEvent();
    final ArgumentCaptor<PaymentCreatedEvent> argumentCaptor = ArgumentCaptor.forClass(PaymentCreatedEvent.class);
    doAnswer(invocationOnMock -> {
      final Payment payment = Payment.class.cast(invocationOnMock.getArguments()[0]).toBuilder().id(1L).build();
      return Uni.createFrom().item(payment);
    }).when(this.paymentRepository).create(any());

    //When
    this.eventConsumer.consume(event);

    //Then
    verify(this.eventPublisher).tryPublish(argumentCaptor.capture());
    final PaymentCreatedEvent result = argumentCaptor.getValue();
    assertThat(result, is(notNullValue()));
    assertThat(result.getId(), is(1L));
    assertThat(result.getPurchaseId(), is(event.getPurchaseId()));
    assertThat(result.getUserId(), is(event.getUserId()));
    assertThat(result.getProductId(), is(event.getProductId()));
    assertThat(result.getCreatedAt(), is(event.getSentAt()));
    assertThat(result.getPrice(), is(event.getPrice()));
    assertThat(result.getStatus(), is("ACCEPTED"));
    assertThat(result.getOperationStatus(), is("SUCCESS"));
  }

  @Test
  public void givenAEventValidCreateWhenInvokeTryPublishThenSendEventError() throws Exception {
    //Given
    final PaymentCreateCommandEvent event = PaymentMother.createPaymentCreateCommandEvent();
    final ArgumentCaptor<PaymentCreatedEvent> argumentCaptor = ArgumentCaptor.forClass(PaymentCreatedEvent.class);
    when(this.paymentRepository.create(any())).thenReturn(Uni.createFrom().failure(new RuntimeException()));

    //When
    this.eventConsumer.consume(event);

    //Then
    verify(this.eventPublisher).tryPublish(argumentCaptor.capture());
    final PaymentCreatedEvent result = argumentCaptor.getValue();
    assertThat(result, is(notNullValue()));
    assertThat(result.getId(), is(nullValue()));
    assertThat(result.getPurchaseId(), is(event.getPurchaseId()));
    assertThat(result.getUserId(), is(event.getUserId()));
    assertThat(result.getProductId(), is(event.getProductId()));
    assertThat(result.getCreatedAt(), is(event.getSentAt()));
    assertThat(result.getPrice(), is(event.getPrice()));
    assertThat(result.getStatus(), is("ACCEPTED"));
    assertThat(result.getOperationStatus(), is("ERROR"));
  }

}