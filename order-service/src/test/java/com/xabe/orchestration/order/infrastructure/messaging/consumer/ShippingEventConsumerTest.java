package com.xabe.orchestration.order.infrastructure.messaging.consumer;

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

import com.xabe.orchestation.common.infrastructure.Event;
import com.xabe.orchestation.common.infrastructure.event.EventConsumer;
import com.xabe.orchestration.order.domain.entity.OrderAggregate;
import com.xabe.orchestration.order.domain.event.shipping.ShippingCreatedEvent;
import com.xabe.orchestration.order.domain.repository.OrderRepository;
import com.xabe.orchestration.order.infrastructure.OrderMother;
import com.xabe.orchestration.order.infrastructure.messaging.consumer.mapper.MessagingConsumerMapperImpl;
import io.smallrye.mutiny.Uni;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

class ShippingEventConsumerTest {

  private Logger logger;

  private OrderRepository orderRepository;

  private EventConsumer eventConsumer;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.orderRepository = mock(OrderRepository.class);
    this.eventConsumer = new ShippingEventConsumer(this.logger, new MessagingConsumerMapperImpl(), this.orderRepository);
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
    final ShippingCreatedEvent event = OrderMother.createShippingCreatedEvent("SUCCESS");
    final OrderAggregate orderAggregate = OrderAggregate.builder().id(event.getPurchaseId()).createdAt(OffsetDateTime.now()).build();
    when(this.orderRepository.load(event.getPurchaseId())).thenReturn(Uni.createFrom().item(orderAggregate));
    final ArgumentCaptor<OrderAggregate> argumentCaptor = ArgumentCaptor.forClass(OrderAggregate.class);
    when(this.orderRepository.update(argumentCaptor.capture())).thenReturn(Uni.createFrom().item(orderAggregate));

    //When
    this.eventConsumer.consume(event);

    //Then
    final OrderAggregate result = argumentCaptor.getValue();
    assertThat(result, is(notNullValue()));
    assertThat(result.getShipping(), is(notNullValue()));
    assertThat(result.getShipping().getId(), is(event.getId()));
    assertThat(result.getShipping().getStatus().name(), is(event.getStatus()));
    assertThat(result.getShipping().getCreatedAt().toInstant(), is(event.getCreatedAt()));
  }

  @Test
  public void givenAEventValidCreateErrorWhenInvokeConsumeThenNotUpdate() throws Exception {
    //Given
    final ShippingCreatedEvent event = OrderMother.createShippingCreatedEvent("ERROR");

    //When
    this.eventConsumer.consume(event);

    //Then
    verify(this.logger).error(anyString(), any(ShippingCreatedEvent.class));
    verify(this.orderRepository, never()).load(any());
    verify(this.orderRepository, never()).update(any());
  }
}