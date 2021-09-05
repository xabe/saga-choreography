package com.xabe.choreography.order.infrastructure.messaging.consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xabe.choreography.common.infrastructure.Event;
import com.xabe.choreography.common.infrastructure.event.EventConsumer;
import com.xabe.choreography.order.domain.entity.OrderAggregate;
import com.xabe.choreography.order.domain.event.payment.PaymentCreatedEvent;
import com.xabe.choreography.order.domain.repository.OrderRepository;
import com.xabe.choreography.order.infrastructure.OrderMother;
import com.xabe.choreography.order.infrastructure.messaging.consumer.mapper.MessagingConsumerMapperImpl;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

class PaymentEventConsumerTest {

  private Logger logger;

  private OrderRepository orderRepository;

  private EventConsumer eventConsumer;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.orderRepository = mock(OrderRepository.class);
    this.eventConsumer = new PaymentEventConsumer(this.logger, new MessagingConsumerMapperImpl(), this.orderRepository);
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
    verify(this.orderRepository, never()).update(any());
  }

  @Test
  public void givenAEventValidCreateWhenInvokeConsumeThenUpdateEntity() throws Exception {
    //Given
    final PaymentCreatedEvent event = OrderMother.createPaymentCreatedEvent("SUCCESS");
    final OrderAggregate orderAggregate = OrderAggregate.builder().id(event.getPurchaseId()).build();
    when(this.orderRepository.load(event.getPurchaseId())).thenReturn(Uni.createFrom().item(orderAggregate));
    final ArgumentCaptor<OrderAggregate> argumentCaptor = ArgumentCaptor.forClass(OrderAggregate.class);
    when(this.orderRepository.update(argumentCaptor.capture())).thenReturn(Uni.createFrom().item(orderAggregate));

    //When
    this.eventConsumer.consume(event);

    //Then
    final OrderAggregate result = argumentCaptor.getValue();
    assertThat(result, is(notNullValue()));
    assertThat(result.getPayment(), is(notNullValue()));
    assertThat(result.getPayment().getId(), is(event.getId()));
    assertThat(result.getPayment().getStatus().name(), is(event.getStatus()));
    assertThat(result.getPayment().getCreatedAt().toInstant(), is(event.getCreatedAt()));
  }

  @Test
  public void givenAEventValidCreateErrorWhenInvokeConsumeThenNotUpdate() throws Exception {
    //Given
    final PaymentCreatedEvent event = OrderMother.createPaymentCreatedEvent("ERROR");

    //When
    this.eventConsumer.consume(event);

    //Then
    verify(this.logger).error(anyString(), any(PaymentCreatedEvent.class));
    verify(this.orderRepository, never()).load(any());
    verify(this.orderRepository, never()).update(any());
  }
}