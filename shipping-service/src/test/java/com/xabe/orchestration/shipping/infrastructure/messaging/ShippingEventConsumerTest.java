package com.xabe.orchestration.shipping.infrastructure.messaging;

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

import com.xabe.orchestation.common.infrastructure.Event;
import com.xabe.orchestation.common.infrastructure.event.EventConsumer;
import com.xabe.orchestation.common.infrastructure.event.EventPublisher;
import com.xabe.orchestration.shipping.domain.entity.Shipping;
import com.xabe.orchestration.shipping.domain.event.ShippingCreateCommandEvent;
import com.xabe.orchestration.shipping.domain.event.ShippingCreatedEvent;
import com.xabe.orchestration.shipping.domain.repository.ShippingRepository;
import com.xabe.orchestration.shipping.infrastructure.ShippingMother;
import com.xabe.orchestration.shipping.infrastructure.messaging.mapper.MessagingMapperImpl;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

class ShippingEventConsumerTest {

  private Logger logger;

  private ShippingRepository shippingRepository;

  private EventPublisher eventPublisher;

  private EventConsumer eventConsumer;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.shippingRepository = mock(ShippingRepository.class);
    this.eventPublisher = mock(EventPublisher.class);
    this.eventConsumer = new ShippingEventConsumer(this.logger, this.shippingRepository, new MessagingMapperImpl(), this.eventPublisher);
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
    final ShippingCreateCommandEvent event = ShippingMother.createPaymentCreateCommandEvent();
    final ArgumentCaptor<ShippingCreatedEvent> argumentCaptor = ArgumentCaptor.forClass(ShippingCreatedEvent.class);
    doAnswer(invocationOnMock -> {
      final Shipping shipping = Shipping.class.cast(invocationOnMock.getArguments()[0]).toBuilder().id(1L).build();
      return Uni.createFrom().item(shipping);
    }).when(this.shippingRepository).create(any());

    //When
    this.eventConsumer.consume(event);

    //Then
    verify(this.eventPublisher).tryPublish(argumentCaptor.capture());
    final ShippingCreatedEvent result = argumentCaptor.getValue();
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
    final ShippingCreateCommandEvent event = ShippingMother.createPaymentCreateCommandEvent();
    final ArgumentCaptor<ShippingCreatedEvent> argumentCaptor = ArgumentCaptor.forClass(ShippingCreatedEvent.class);
    when(this.shippingRepository.create(any())).thenReturn(Uni.createFrom().failure(new RuntimeException()));

    //When
    this.eventConsumer.consume(event);

    //Then
    verify(this.eventPublisher).tryPublish(argumentCaptor.capture());
    final ShippingCreatedEvent result = argumentCaptor.getValue();
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