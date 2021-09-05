package com.xabe.orchestration.order.infrastructure.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xabe.orchestation.common.infrastructure.dispatch.CommandDispatcher;
import com.xabe.orchestration.order.domain.entity.OrderAggregate;
import com.xabe.orchestration.order.domain.repository.OrderRepository;
import io.smallrye.mutiny.Uni;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderUseCaseImplTest {

  private OrderRepository orderRepository;

  private OrderUseCase orderUseCase;

  private CommandDispatcher commandDispatcher;

  @BeforeEach
  public void setUp() throws Exception {
    this.orderRepository = mock(OrderRepository.class);
    this.commandDispatcher = mock(CommandDispatcher.class);
    this.orderUseCase = new OrderUseCaseImpl(this.orderRepository, this.commandDispatcher);
  }

  @Test
  public void givenAIdWhenInvokeGetOrderThenReturnOrden() throws Exception {
    //Given
    final String id = "id";
    when(this.orderRepository.load(id)).thenReturn(Uni.createFrom().item(OrderAggregate::new));

    //When
    final Uni<OrderAggregate> order = this.orderUseCase.getOrder(id);

    //Then
    assertThat(order, is(notNullValue()));
    final OrderAggregate result = order.subscribeAsCompletionStage().get();
    assertThat(result, is(notNullValue()));
  }

  @Test
  public void shouldGetAllOrders() throws Exception {
    //Given
    when(this.orderRepository.getAll()).thenReturn(Uni.createFrom().item(List.of(OrderAggregate.builder().build())));

    //When
    final Uni<List<OrderAggregate>> orders = this.orderUseCase.getOrders();

    //Then
    assertThat(orders, is(notNullValue()));
    final List<OrderAggregate> result = orders.subscribeAsCompletionStage().get();
    assertThat(result, is(notNullValue()));
    assertThat(result, is(hasSize(1)));
  }

  @Test
  public void shouldCreateOrder() throws Exception {
    //Given
    final OrderAggregate orderAggregate = OrderAggregate.builder().build();
    when(this.orderRepository.save(orderAggregate)).thenReturn(Uni.createFrom().item(orderAggregate));
    when(this.commandDispatcher.dispatch(any())).thenReturn(Uni.createFrom().item(orderAggregate));

    //When
    final Uni<OrderAggregate> result = this.orderUseCase.create(orderAggregate);

    //Then
    assertThat(result, is(notNullValue()));
    assertThat(result.subscribeAsCompletionStage().get(), is(notNullValue()));
    verify(this.commandDispatcher).dispatch(any());
  }

}