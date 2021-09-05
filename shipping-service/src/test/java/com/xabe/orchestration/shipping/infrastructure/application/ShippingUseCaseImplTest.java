package com.xabe.orchestration.shipping.infrastructure.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xabe.orchestration.shipping.domain.entity.Shipping;
import com.xabe.orchestration.shipping.domain.repository.ShippingRepository;
import io.smallrye.mutiny.Uni;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShippingUseCaseImplTest {

  private ShippingRepository shippingRepository;

  private ShippingUseCase shippingUseCase;

  @BeforeEach
  public void setUp() throws Exception {
    this.shippingRepository = mock(ShippingRepository.class);
    this.shippingUseCase = new ShippingUseCaseImpl(this.shippingRepository);
  }

  @Test
  public void givenAIdWhenInvokeGetShippingThenReturnShipping() throws Exception {
    //Given
    final Long id = 1L;
    when(this.shippingRepository.getShipping(id)).thenReturn(Uni.createFrom().item(Shipping.builder().build()));

    //When
    final Uni<Shipping> shipping = this.shippingUseCase.getShipping(id);

    //Then
    assertThat(shipping, is(notNullValue()));
    final Shipping result = shipping.subscribeAsCompletionStage().get();
    assertThat(result, is(notNullValue()));
  }

  @Test
  public void shouldGetAllShipments() throws Exception {
    //Given
    when(this.shippingRepository.getShipments()).thenReturn(Uni.createFrom().item(List.of(Shipping.builder().build())));

    //When
    final Uni<List<Shipping>> shipments = this.shippingUseCase.getShipments();

    //Then
    assertThat(shipments, is(notNullValue()));
    final List<Shipping> result = shipments.subscribeAsCompletionStage().get();
    assertThat(result, is(notNullValue()));
    assertThat(result, is(hasSize(1)));
  }

  @Test
  public void shouldCreateShipping() throws Exception {
    //Given
    final Shipping shipping = Shipping.builder().build();
    when(this.shippingRepository.create(any())).thenReturn(Uni.createFrom().item(shipping));

    //When
    final Uni<Shipping> result = this.shippingUseCase.create(shipping);

    //Then
    assertThat(result, is(notNullValue()));
    assertThat(result.subscribeAsCompletionStage().get(), is(notNullValue()));
  }

  @Test
  public void shouldUpdateShipping() throws Exception {
    //Given
    final Shipping shipping = Shipping.builder().build();
    final Long id = 1L;
    when(this.shippingRepository.update(id, shipping)).thenReturn(Uni.createFrom().item(shipping));

    //When
    final Uni<Shipping> result = this.shippingUseCase.update(id, shipping);

    //Then
    assertThat(result, is(notNullValue()));
    assertThat(result.subscribeAsCompletionStage().get(), is(notNullValue()));
  }
}