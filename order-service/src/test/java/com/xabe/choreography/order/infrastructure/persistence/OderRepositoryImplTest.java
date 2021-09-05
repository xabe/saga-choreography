package com.xabe.choreography.order.infrastructure.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xabe.choreography.common.infrastructure.exception.EntityNotFoundException;
import com.xabe.choreography.order.domain.entity.OrderAggregate;
import com.xabe.choreography.order.domain.repository.OrderRepository;
import com.xabe.choreography.order.infrastructure.OrderMother;
import com.xabe.choreography.order.infrastructure.persistence.dto.OrderDTO;
import com.xabe.choreography.order.infrastructure.persistence.mapper.PersistenceMapperImpl;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

class OderRepositoryImplTest {

  private OrderRepositoryPanache orderRepositoryPanache;

  private OrderRepository orderRepository;

  @BeforeEach
  public void setUp() throws Exception {
    final Logger logger = mock(Logger.class);
    this.orderRepositoryPanache = mock(OrderRepositoryPanache.class);
    this.orderRepository = new OderRepositoryImpl(logger, new PersistenceMapperImpl(), this.orderRepositoryPanache);
  }

  @Test
  public void shouldGetOrder() throws Exception {
    //Given
    final String id = "1";
    final OrderDTO orderDTO = OrderMother.createOrderDTO();
    final PanacheQuery panacheQuery = mock(PanacheQuery.class);
    when(this.orderRepositoryPanache.find("purchaseId", id)).thenReturn(panacheQuery);
    when(panacheQuery.firstResult()).thenReturn(Uni.createFrom().item(orderDTO));

    //When
    final Uni<OrderAggregate> result = this.orderRepository.load(id);

    //Then
    assertThat(result, is(notNullValue()));
    final OrderAggregate orderAggregate = result.subscribeAsCompletionStage().get();
    assertThat(orderAggregate.getId(), is(orderDTO.getPurchaseId()));
    assertThat(orderAggregate.getUserId(), is(orderDTO.getUserId()));
    assertThat(orderAggregate.getProductId(), is(orderDTO.getProductId()));
    assertThat(orderAggregate.getPrice(), is(orderDTO.getPrice().get()));
    assertThat(orderAggregate.getStatus().name(), is(orderDTO.getStatus().name()));
    assertThat(orderAggregate.getCreatedAt(), is(orderDTO.getCreatedAt()));
    assertThat(orderAggregate.getOrder().getId(), is(orderDTO.getId()));
    assertThat(orderAggregate.getOrder().getStatus().name(), is(orderDTO.getOrderStatus().name()));
    assertThat(orderAggregate.getOrder().getCreatedAt(), is(orderDTO.getCreatedAt()));
    assertThat(orderAggregate.getPayment().getId(), is(orderDTO.getPaymentId()));
    assertThat(orderAggregate.getPayment().getStatus().name(), is(orderDTO.getPaymentStatus().name()));
    assertThat(orderAggregate.getPayment().getCreatedAt(), is(orderDTO.getPaymentCreatedAt()));
    MatcherAssert.assertThat(orderAggregate.getShipping().getId(), is(orderDTO.getShippingId()));
    assertThat(orderAggregate.getShipping().getStatus().name(), is(orderDTO.getShippingStatus().name()));
    MatcherAssert.assertThat(orderAggregate.getShipping().getCreatedAt(), is(orderDTO.getShippingCreatedAt()));
  }

  @Test
  public void shouldGetAllOrders() throws Exception {
    //Given
    final OrderDTO orderDTO = OrderMother.createOrderDTO();
    when(this.orderRepositoryPanache.listAll()).thenReturn(Uni.createFrom().item(List.of(orderDTO)));

    //When
    final Uni<List<OrderAggregate>> result = this.orderRepository.getAll();

    //Then
    assertThat(result, is(notNullValue()));
    final List<OrderAggregate> orders = result.subscribeAsCompletionStage().get();
    assertThat(orders, is(notNullValue()));
    assertThat(orders, is(hasSize(1)));
    final OrderAggregate orderAggregate = orders.get(0);
    assertThat(orderAggregate.getId(), is(orderDTO.getPurchaseId()));
    assertThat(orderAggregate.getUserId(), is(orderDTO.getUserId()));
    assertThat(orderAggregate.getProductId(), is(orderDTO.getProductId()));
    assertThat(orderAggregate.getPrice(), is(orderDTO.getPrice().get()));
    assertThat(orderAggregate.getStatus().name(), is(orderDTO.getStatus().name()));
    assertThat(orderAggregate.getCreatedAt(), is(orderDTO.getCreatedAt()));
    assertThat(orderAggregate.getOrder().getId(), is(orderDTO.getId()));
    assertThat(orderAggregate.getOrder().getStatus().name(), is(orderDTO.getOrderStatus().name()));
    assertThat(orderAggregate.getOrder().getCreatedAt(), is(orderDTO.getCreatedAt()));
    assertThat(orderAggregate.getPayment().getId(), is(orderDTO.getPaymentId()));
    assertThat(orderAggregate.getPayment().getStatus().name(), is(orderDTO.getPaymentStatus().name()));
    assertThat(orderAggregate.getPayment().getCreatedAt(), is(orderDTO.getPaymentCreatedAt()));
    MatcherAssert.assertThat(orderAggregate.getShipping().getId(), is(orderDTO.getShippingId()));
    assertThat(orderAggregate.getShipping().getStatus().name(), is(orderDTO.getShippingStatus().name()));
    MatcherAssert.assertThat(orderAggregate.getShipping().getCreatedAt(), is(orderDTO.getShippingCreatedAt()));
  }

  @Test
  public void shouldCreateOrder() throws Exception {
    //Given
    final OrderAggregate orderAggregate = OrderMother.createOrderAggregate();
    final OrderDTO orderDTO = OrderMother.createOrderDTO();
    final ArgumentCaptor<OrderDTO> argumentCaptor = ArgumentCaptor.forClass(OrderDTO.class);
    when(this.orderRepositoryPanache.persistAndFlush(argumentCaptor.capture())).thenReturn(Uni.createFrom().item(orderDTO));

    //When
    final Uni<OrderAggregate> result = this.orderRepository.save(orderAggregate);

    //Then
    assertThat(result, is(notNullValue()));
    final OrderAggregate orderAggregateResult = result.subscribeAsCompletionStage().get();
    assertThat(orderAggregateResult.getId(), is(orderDTO.getPurchaseId()));
    assertThat(orderAggregateResult.getUserId(), is(orderDTO.getUserId()));
    assertThat(orderAggregateResult.getProductId(), is(orderDTO.getProductId()));
    assertThat(orderAggregateResult.getPrice(), is(orderDTO.getPrice().get()));
    assertThat(orderAggregateResult.getStatus().name(), is(orderDTO.getStatus().name()));
    assertThat(orderAggregateResult.getCreatedAt(), is(orderDTO.getCreatedAt()));
    assertThat(orderAggregateResult.getOrder().getId(), is(orderDTO.getId()));
    assertThat(orderAggregateResult.getOrder().getStatus().name(), is(orderDTO.getOrderStatus().name()));
    assertThat(orderAggregateResult.getOrder().getCreatedAt(), is(orderDTO.getCreatedAt()));
    assertThat(orderAggregateResult.getPayment().getId(), is(orderDTO.getPaymentId()));
    assertThat(orderAggregateResult.getPayment().getStatus().name(), is(orderDTO.getPaymentStatus().name()));
    assertThat(orderAggregateResult.getPayment().getCreatedAt(), is(orderDTO.getPaymentCreatedAt()));
    MatcherAssert.assertThat(orderAggregateResult.getShipping().getId(), is(orderDTO.getShippingId()));
    assertThat(orderAggregateResult.getShipping().getStatus().name(), is(orderDTO.getShippingStatus().name()));
    MatcherAssert.assertThat(orderAggregateResult.getShipping().getCreatedAt(), is(orderDTO.getShippingCreatedAt()));
    final OrderDTO value = argumentCaptor.getValue();
    assertThat(value.getId(), is(orderAggregate.getOrder().getId()));
    assertThat(value.getPurchaseId(), is(orderAggregate.getId()));
    assertThat(value.getUserId(), is(orderAggregate.getUserId()));
    assertThat(value.getProductId(), is(orderAggregate.getProductId()));
    assertThat(value.getPrice().get(), is(orderAggregate.getPrice()));
    assertThat(value.getStatus().name(), is(orderAggregate.getStatus().name()));
    assertThat(value.getCreatedAt(), is(orderAggregate.getCreatedAt()));
    assertThat(value.getOrderStatus().name(), is(orderAggregate.getOrder().getStatus().name()));
    assertThat(value.getPaymentId(), is(orderAggregate.getPayment().getId()));
    assertThat(value.getPaymentStatus().name(), is(orderAggregate.getPayment().getStatus().name()));
    assertThat(value.getPaymentCreatedAt(), is(orderAggregate.getPayment().getCreatedAt()));
    MatcherAssert.assertThat(value.getShippingId(), Matchers.is(orderAggregate.getShipping().getId()));
    assertThat(value.getShippingStatus().name(), is(orderAggregate.getShipping().getStatus().name()));
    MatcherAssert.assertThat(value.getShippingCreatedAt(), Matchers.is(orderAggregate.getShipping().getCreatedAt()));
  }

  @Test
  public void shouldUpdateOrder() throws Exception {
    //Given
    final OrderAggregate orderAggregate = OrderMother.createOrderAggregate();
    final OrderDTO orderDTO = OrderMother.createOrderDTO();
    final ArgumentCaptor<OrderDTO> argumentCaptor = ArgumentCaptor.forClass(OrderDTO.class);
    final PanacheQuery panacheQuery = mock(PanacheQuery.class);
    when(this.orderRepositoryPanache.find("purchaseId", orderAggregate.getId())).thenReturn(panacheQuery);
    when(panacheQuery.firstResult()).thenReturn(Uni.createFrom().item(orderDTO));
    when(this.orderRepositoryPanache.persistAndFlush(argumentCaptor.capture())).thenReturn(Uni.createFrom().item(orderDTO));

    //When
    final Uni<OrderAggregate> result = this.orderRepository.update(orderAggregate);

    //Then
    assertThat(result, is(notNullValue()));
    final OrderAggregate orderAggregateResult = result.subscribeAsCompletionStage().get();
    assertThat(orderAggregateResult.getId(), is(orderDTO.getPurchaseId()));
    assertThat(orderAggregateResult.getUserId(), is(orderDTO.getUserId()));
    assertThat(orderAggregateResult.getProductId(), is(orderDTO.getProductId()));
    assertThat(orderAggregateResult.getPrice(), is(orderDTO.getPrice().get()));
    assertThat(orderAggregateResult.getStatus().name(), is(orderDTO.getStatus().name()));
    assertThat(orderAggregateResult.getCreatedAt(), is(orderDTO.getCreatedAt()));
    assertThat(orderAggregateResult.getOrder().getId(), is(orderDTO.getId()));
    assertThat(orderAggregateResult.getOrder().getStatus().name(), is(orderDTO.getOrderStatus().name()));
    assertThat(orderAggregateResult.getOrder().getCreatedAt(), is(orderDTO.getCreatedAt()));
    assertThat(orderAggregateResult.getPayment().getId(), is(orderDTO.getPaymentId()));
    assertThat(orderAggregateResult.getPayment().getStatus().name(), is(orderDTO.getPaymentStatus().name()));
    assertThat(orderAggregateResult.getPayment().getCreatedAt(), is(orderDTO.getPaymentCreatedAt()));
    MatcherAssert.assertThat(orderAggregateResult.getShipping().getId(), is(orderDTO.getShippingId()));
    assertThat(orderAggregateResult.getShipping().getStatus().name(), is(orderDTO.getShippingStatus().name()));
    MatcherAssert.assertThat(orderAggregateResult.getShipping().getCreatedAt(), is(orderDTO.getShippingCreatedAt()));
    final OrderDTO value = argumentCaptor.getValue();
    assertThat(value.getId(), is(orderAggregate.getOrder().getId()));
    assertThat(value.getPurchaseId(), is(orderAggregate.getId()));
    assertThat(value.getUserId(), is(orderAggregate.getUserId()));
    assertThat(value.getProductId(), is(orderAggregate.getProductId()));
    assertThat(value.getPrice().get(), is(orderAggregate.getPrice()));
    assertThat(value.getStatus().name(), is(orderAggregate.getStatus().name()));
    assertThat(value.getCreatedAt(), is(orderAggregate.getCreatedAt()));
    assertThat(value.getOrderStatus().name(), is(orderAggregate.getOrder().getStatus().name()));
    assertThat(value.getPaymentId(), is(orderAggregate.getPayment().getId()));
    assertThat(value.getPaymentStatus().name(), is(orderAggregate.getPayment().getStatus().name()));
    assertThat(value.getPaymentCreatedAt(), is(orderAggregate.getPayment().getCreatedAt()));
    MatcherAssert.assertThat(value.getShippingId(), Matchers.is(orderAggregate.getShipping().getId()));
    assertThat(value.getShippingStatus().name(), is(orderAggregate.getShipping().getStatus().name()));
    MatcherAssert.assertThat(value.getShippingCreatedAt(), Matchers.is(orderAggregate.getShipping().getCreatedAt()));
  }

  @Test
  public void shouldNotUpdateOrder() throws Exception {
    //Given
    final OrderAggregate orderAggregate = OrderMother.createOrderAggregate();
    final PanacheQuery panacheQuery = mock(PanacheQuery.class);
    when(this.orderRepositoryPanache.find("purchaseId", orderAggregate.getId())).thenReturn(panacheQuery);
    when(panacheQuery.firstResult()).thenReturn(Uni.createFrom().nullItem());

    //When
    final UniAssertSubscriber<OrderAggregate> result =
        this.orderRepository.update(orderAggregate).subscribe().withSubscriber(UniAssertSubscriber.create());

    //Then
    assertThat(result, is(notNullValue()));
    result.awaitFailure();
    result.assertFailedWith(EntityNotFoundException.class);
  }

}