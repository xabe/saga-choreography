package com.xabe.orchestration.shipping.infrastructure.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xabe.orchestration.shipping.infrastructure.ShippingMother;
import com.xabe.orchestration.shipping.domain.entity.Shipping;
import com.xabe.orchestration.shipping.domain.repository.ShippingRepository;
import com.xabe.orchestration.shipping.infrastructure.persistence.dto.ShippingDTO;
import com.xabe.orchestration.shipping.infrastructure.persistence.mapper.PersistenceMapperImpl;
import io.smallrye.mutiny.Uni;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

class ShippingRepositoryImplTest {

  private ShippingRepositoryPanache shippingRepositoryPanache;

  private ShippingRepository shippingRepository;

  @BeforeEach
  public void setUp() throws Exception {
    final Logger logger = mock(Logger.class);
    this.shippingRepositoryPanache = mock(ShippingRepositoryPanache.class);
    this.shippingRepository = new ShippingRepositoryImpl(logger, new PersistenceMapperImpl(), this.shippingRepositoryPanache);
  }

  @Test
  public void shouldGetShipping() throws Exception {
    //Given
    final Long id = 1L;
    final ShippingDTO shippingDTO = ShippingMother.createShippingDTO();
    when(this.shippingRepositoryPanache.findById(id)).thenReturn(Uni.createFrom().item(shippingDTO));

    //When
    final Uni<Shipping> result = this.shippingRepository.getShipping(id);

    //Then
    assertThat(result, is(notNullValue()));
    final Shipping shipping = result.subscribeAsCompletionStage().get();
    assertThat(shipping.getId(), is(shippingDTO.getId()));
    assertThat(shipping.getPurchaseId(), is(shippingDTO.getPurchaseId()));
    assertThat(shipping.getUserId(), is(shippingDTO.getUserId()));
    assertThat(shipping.getProductId(), is(shippingDTO.getProductId()));
    assertThat(shipping.getPrice(), is(shippingDTO.getPrice().get()));
    assertThat(shipping.getStatus().name(), is(shippingDTO.getStatus().name()));
    assertThat(shipping.getCreatedAt(), is(shippingDTO.getCreatedAt()));
  }

  @Test
  public void shouldGetAllShipments() throws Exception {
    //Given
    final ShippingDTO shippingDTO = ShippingMother.createShippingDTO();
    when(this.shippingRepositoryPanache.listAll()).thenReturn(Uni.createFrom().item(List.of(shippingDTO)));

    //When
    final Uni<List<Shipping>> result = this.shippingRepository.getShipments();

    //Then
    assertThat(result, is(notNullValue()));
    final List<Shipping> shippings = result.subscribeAsCompletionStage().get();
    assertThat(shippings, is(notNullValue()));
    assertThat(shippings, is(hasSize(1)));
    final Shipping shipping = shippings.get(0);
    assertThat(shipping.getId(), is(shippingDTO.getId()));
    assertThat(shipping.getPurchaseId(), is(shippingDTO.getPurchaseId()));
    assertThat(shipping.getUserId(), is(shippingDTO.getUserId()));
    assertThat(shipping.getProductId(), is(shippingDTO.getProductId()));
    assertThat(shipping.getPrice(), is(shippingDTO.getPrice().get()));
    assertThat(shipping.getStatus().name(), is(shippingDTO.getStatus().name()));
    assertThat(shipping.getCreatedAt(), is(shippingDTO.getCreatedAt()));
  }

  @Test
  public void shouldCreateShipping() throws Exception {
    //Given
    final Shipping shipping = ShippingMother.createShipping();
    final ShippingDTO shippingDTO = ShippingMother.createShippingDTO();
    final ArgumentCaptor<ShippingDTO> argumentCaptor = ArgumentCaptor.forClass(ShippingDTO.class);
    when(this.shippingRepositoryPanache.persistAndFlush(argumentCaptor.capture())).thenReturn(Uni.createFrom().item(shippingDTO));

    //When
    final Uni<Shipping> result = this.shippingRepository.create(shipping);

    //Then
    assertThat(result, is(notNullValue()));
    final Shipping shippingResult = result.subscribeAsCompletionStage().get();
    assertThat(shippingResult.getId(), is(shippingDTO.getId()));
    assertThat(shippingResult.getPurchaseId(), is(shippingDTO.getPurchaseId()));
    assertThat(shippingResult.getUserId(), is(shippingDTO.getUserId()));
    assertThat(shippingResult.getProductId(), is(shippingDTO.getProductId()));
    assertThat(shippingResult.getPrice(), is(shippingDTO.getPrice().get()));
    assertThat(shippingResult.getStatus().name(), is(shippingDTO.getStatus().name()));
    assertThat(shippingResult.getCreatedAt(), is(shippingDTO.getCreatedAt()));
    final ShippingDTO value = argumentCaptor.getValue();
    assertThat(value.getId(), is(nullValue()));
    assertThat(value.getPurchaseId(), is(shipping.getPurchaseId()));
    assertThat(value.getUserId(), is(shipping.getUserId()));
    assertThat(value.getProductId(), is(shipping.getProductId()));
    assertThat(value.getPrice().get(), is(shipping.getPrice()));
    assertThat(value.getStatus().name(), is(shipping.getStatus().name()));
    assertThat(value.getCreatedAt(), is(shipping.getCreatedAt()));
  }

  @Test
  public void shouldUpdateShippingNew() throws Exception {
    //Given
    final Long id = 1L;
    final Shipping shipping = ShippingMother.createShipping();
    final ShippingDTO shippingDTO = ShippingMother.createShippingDTO();
    final ArgumentCaptor<ShippingDTO> argumentCaptor = ArgumentCaptor.forClass(ShippingDTO.class);
    when(this.shippingRepositoryPanache.findById(id)).thenReturn(Uni.createFrom().nullItem());
    when(this.shippingRepositoryPanache.persistAndFlush(argumentCaptor.capture())).thenReturn(Uni.createFrom().item(shippingDTO));

    //When
    final Uni<Shipping> result = this.shippingRepository.update(id, shipping);

    //Then
    assertThat(result, is(notNullValue()));
    final Shipping shippingResult = result.subscribeAsCompletionStage().get();
    assertThat(shippingResult.getId(), is(shippingDTO.getId()));
    assertThat(shippingResult.getPurchaseId(), is(shippingDTO.getPurchaseId()));
    assertThat(shippingResult.getUserId(), is(shippingDTO.getUserId()));
    assertThat(shippingResult.getProductId(), is(shippingDTO.getProductId()));
    assertThat(shippingResult.getPrice(), is(shippingDTO.getPrice().get()));
    assertThat(shippingResult.getStatus().name(), is(shippingDTO.getStatus().name()));
    assertThat(shippingResult.getCreatedAt(), is(shippingDTO.getCreatedAt()));
    final ShippingDTO value = argumentCaptor.getValue();
    assertThat(value.getId(), is(nullValue()));
    assertThat(value.getPurchaseId(), is(shipping.getPurchaseId()));
    assertThat(value.getUserId(), is(shipping.getUserId()));
    assertThat(value.getProductId(), is(shipping.getProductId()));
    assertThat(value.getPrice().get(), is(shipping.getPrice()));
    assertThat(value.getStatus().name(), is(shipping.getStatus().name()));
    assertThat(value.getCreatedAt(), is(shipping.getCreatedAt()));
  }

  @Test
  public void shouldUpdateShippingOld() throws Exception {
    //Given
    final Long id = 1L;
    final Shipping shipping = ShippingMother.createShippingNew();
    final ShippingDTO shippingDTO = ShippingMother.createShippingDTO();
    final ArgumentCaptor<ShippingDTO> argumentCaptor = ArgumentCaptor.forClass(ShippingDTO.class);
    when(this.shippingRepositoryPanache.findById(id)).thenReturn(Uni.createFrom().item(shippingDTO));
    when(this.shippingRepositoryPanache.persistAndFlush(argumentCaptor.capture())).thenReturn(Uni.createFrom().item(shippingDTO));

    //When
    final Uni<Shipping> result = this.shippingRepository.update(id, shipping);

    //Then
    assertThat(result, is(notNullValue()));
    final Shipping shippingResult = result.subscribeAsCompletionStage().get();
    assertThat(shippingResult.getId(), is(shippingDTO.getId()));
    assertThat(shippingResult.getPurchaseId(), is(shippingDTO.getPurchaseId()));
    assertThat(shippingResult.getUserId(), is(shippingDTO.getUserId()));
    assertThat(shippingResult.getProductId(), is(shippingDTO.getProductId()));
    assertThat(shippingResult.getPrice(), is(shippingDTO.getPrice().get()));
    assertThat(shippingResult.getStatus().name(), is(shippingDTO.getStatus().name()));
    assertThat(shippingResult.getCreatedAt(), is(shippingDTO.getCreatedAt()));
    final ShippingDTO value = argumentCaptor.getValue();
    assertThat(value.getId(), is(shipping.getId()));
    assertThat(value.getPurchaseId(), is(shipping.getPurchaseId()));
    assertThat(value.getUserId(), is(shipping.getUserId()));
    assertThat(value.getProductId(), is(shipping.getProductId()));
    assertThat(value.getPrice().get(), is(shipping.getPrice()));
    assertThat(value.getStatus().name(), is(shipping.getStatus().name()));
    assertThat(value.getCreatedAt(), is(shipping.getCreatedAt()));
  }

}