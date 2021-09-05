package com.xabe.orchestration.shipping.infrastructure.presentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xabe.orchestration.shipping.domain.entity.Shipping;
import com.xabe.orchestration.shipping.infrastructure.ShippingMother;
import com.xabe.orchestration.shipping.infrastructure.application.ShippingUseCase;
import com.xabe.orchestration.shipping.infrastructure.presentation.mapper.PresentationMapperImpl;
import com.xabe.orchestration.shipping.infrastructure.presentation.payload.ShippingPayload;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.time.OffsetDateTime;
import java.util.List;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.common.jaxrs.UriBuilderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShippingResourceTest {

  private ShippingUseCase shippingUseCase;

  private ShippingResource shippingResource;

  @BeforeEach
  public void setUp() throws Exception {
    this.shippingUseCase = mock(ShippingUseCase.class);
    this.shippingResource = new ShippingResource(this.shippingUseCase, new PresentationMapperImpl());
  }

  @Test
  public void shouldGetAllShipments() throws Exception {
    //Given
    final Shipping shipping = Shipping.builder()
        .id(1L)
        .purchaseId("111")
        .price(1L)
        .productId("1")
        .userId("2")
        .createdAt(OffsetDateTime.now()).build();
    when(this.shippingUseCase.getShipments()).thenReturn(Uni.createFrom().item(List.of(shipping)));

    //When
    final Uni<List<ShippingPayload>> result = this.shippingResource.getShipments();

    //Then
    assertThat(result, is(notNullValue()));
    final List<ShippingPayload> payloads = result.subscribeAsCompletionStage().get();
    assertThat(payloads, is(notNullValue()));
    assertThat(payloads, is(hasSize(1)));
    final ShippingPayload shippingPayload = payloads.get(0);
    assertThat(shippingPayload.getId(), is(shipping.getId()));
    assertThat(shippingPayload.getPurchaseId(), is(shipping.getPurchaseId()));
    assertThat(shippingPayload.getUserId(), is(shipping.getUserId()));
    assertThat(shippingPayload.getProductId(), is(shipping.getProductId()));
    assertThat(shippingPayload.getPrice(), is(shipping.getPrice()));
    assertThat(shippingPayload.getStatus().name(), is(shipping.getStatus().name()));
    assertThat(shippingPayload.getCreatedAt(), is(shipping.getCreatedAt()));
  }

  @Test
  public void shouldGetShipping() throws Exception {
    //Given
    final Long id = 1L;
    final Shipping shipping = ShippingMother.createShipping();
    when(this.shippingUseCase.getShipping(id)).thenReturn(Uni.createFrom().item(shipping));

    //When
    final Uni<ShippingPayload> result = this.shippingResource.getShipping(id);

    //Then
    assertThat(result, is(notNullValue()));
    final ShippingPayload shippingPayload = result.subscribeAsCompletionStage().get();
    assertThat(shippingPayload, is(notNullValue()));
    assertThat(shippingPayload.getId(), is(shipping.getId()));
    assertThat(shippingPayload.getPurchaseId(), is(shipping.getPurchaseId()));
    assertThat(shippingPayload.getUserId(), is(shipping.getUserId()));
    assertThat(shippingPayload.getProductId(), is(shipping.getProductId()));
    assertThat(shippingPayload.getPrice(), is(shipping.getPrice()));
    assertThat(shippingPayload.getStatus().name(), is(shipping.getStatus().name()));
    assertThat(shippingPayload.getCreatedAt(), is(shipping.getCreatedAt()));
  }

  @Test
  public void shouldGetErrorShipping() throws Exception {
    //Given
    final Long id = 1L;
    when(this.shippingUseCase.getShipping(id)).thenReturn(Uni.createFrom().nullItem());

    //When
    final UniAssertSubscriber<ShippingPayload> result =
        this.shippingResource.getShipping(id).subscribe().withSubscriber(UniAssertSubscriber.create());

    //Then
    assertThat(result, is(notNullValue()));
    result.awaitFailure();
    result.assertFailedWith(NotFoundException.class);
  }

  @Test
  public void shouldCreatedShipping() throws Exception {
    //Given
    final ShippingPayload shippingPayload = ShippingPayload.builder().productId("1").userId("2").price(1L).build();
    final Shipping shipping = Shipping.builder().productId("1").userId("2").price(1L).build();
    final UriInfo uriInfo = mock(UriInfo.class);
    final UriBuilder uriBuilder = new UriBuilderImpl();
    final Long uuid = 1L;
    when(this.shippingUseCase.create(eq(shipping))).thenReturn(Uni.createFrom().item(shipping.toBuilder().id(1L).build()));
    when(uriInfo.getRequestUriBuilder()).thenReturn(uriBuilder);

    //When
    final Uni<Response> result = this.shippingResource.create(shippingPayload, uriInfo);

    //Then
    assertThat(result, is(notNullValue()));
    final Response response = result.subscribeAsCompletionStage().get();
    assertThat(response.getLocation(), is(notNullValue()));
    assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));
    assertThat(response.getLocation(), is(notNullValue()));
    assertThat(response.getLocation(), is(new UriBuilderImpl().path(uuid.toString()).build()));
  }

  @Test
  public void shouldCreateShippingError() throws Exception {
    //Given
    final ShippingPayload shippingPayload = ShippingPayload.builder().productId("1").userId("2").price(1L).build();
    final Shipping shipping = Shipping.builder().productId("1").userId("2").price(1L).build();
    final UriInfo uriInfo = mock(UriInfo.class);
    final UriBuilder uriBuilder = new UriBuilderImpl();
    when(this.shippingUseCase.create(shipping)).thenReturn(Uni.createFrom().failure(RuntimeException::new));
    when(uriInfo.getRequestUriBuilder()).thenReturn(uriBuilder);

    //When
    final Uni<Response> result = this.shippingResource.create(shippingPayload, uriInfo);

    //Then
    assertThat(result, is(notNullValue()));
    final Response response = result.subscribeAsCompletionStage().get();
    assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void shouldUpdateShipping() throws Exception {
    //Given
    final ShippingPayload shippingPayload = ShippingPayload.builder().productId("1").userId("2").price(1L).build();
    final Shipping shipping = Shipping.builder().productId("1").userId("2").price(1L).build();
    final UriInfo uriInfo = mock(UriInfo.class);
    final UriBuilder uriBuilder = new UriBuilderImpl();
    final Long id = 1L;
    when(this.shippingUseCase.update(id, shipping)).thenReturn(Uni.createFrom().item(shipping.toBuilder().id(1L).build()));
    when(uriInfo.getRequestUriBuilder()).thenReturn(uriBuilder);

    //When
    final Uni<Response> result = this.shippingResource.update(id, shippingPayload, uriInfo);

    //Then
    assertThat(result, is(notNullValue()));
    final Response response = result.subscribeAsCompletionStage().get();
    assertThat(response.getLocation(), is(notNullValue()));
    assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));
    assertThat(response.getLocation(), is(notNullValue()));
    assertThat(response.getLocation(), is(new UriBuilderImpl().build()));
  }

  @Test
  public void shouldUpdatedShippingError() throws Exception {
    //Given
    final ShippingPayload shippingPayload = ShippingPayload.builder().productId("1").userId("2").price(1L).build();
    final Shipping shipping = Shipping.builder().productId("1").userId("2").price(1L).build();
    final UriInfo uriInfo = mock(UriInfo.class);
    final UriBuilder uriBuilder = new UriBuilderImpl();
    final Long id = 1L;
    when(this.shippingUseCase.update(id, shipping)).thenReturn(Uni.createFrom().failure(RuntimeException::new));
    when(uriInfo.getRequestUriBuilder()).thenReturn(uriBuilder);

    //When
    final Uni<Response> result = this.shippingResource.update(id, shippingPayload, uriInfo);

    //Then
    assertThat(result, is(notNullValue()));
    final Response response = result.subscribeAsCompletionStage().get();
    assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
  }
}