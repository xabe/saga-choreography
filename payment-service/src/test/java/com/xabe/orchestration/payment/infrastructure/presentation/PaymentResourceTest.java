package com.xabe.orchestration.payment.infrastructure.presentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xabe.orchestration.payment.infrastructure.PaymentMother;
import com.xabe.orchestration.payment.infrastructure.application.PaymentUseCase;
import com.xabe.orchestration.payment.domain.entity.Payment;
import com.xabe.orchestration.payment.infrastructure.presentation.mapper.PresentationMapperImpl;
import com.xabe.orchestration.payment.infrastructure.presentation.payload.PaymentPayload;
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

class PaymentResourceTest {

  private PaymentUseCase paymentUseCase;

  private PaymentResource paymentResource;

  @BeforeEach
  public void setUp() throws Exception {
    this.paymentUseCase = mock(PaymentUseCase.class);
    this.paymentResource = new PaymentResource(this.paymentUseCase, new PresentationMapperImpl());
  }

  @Test
  public void shouldGetAllPayments() throws Exception {
    //Given
    final Payment payment = Payment.builder()
        .id(1L)
        .purchaseId("111")
        .price(1L)
        .productId("1")
        .userId("2")
        .createdAt(OffsetDateTime.now()).build();
    when(this.paymentUseCase.getPayments()).thenReturn(Uni.createFrom().item(List.of(payment)));

    //When
    final Uni<List<PaymentPayload>> result = this.paymentResource.getPayments();

    //Then
    assertThat(result, is(notNullValue()));
    final List<PaymentPayload> payloads = result.subscribeAsCompletionStage().get();
    assertThat(payloads, is(notNullValue()));
    assertThat(payloads, is(hasSize(1)));
    final PaymentPayload paymentPayload = payloads.get(0);
    assertThat(paymentPayload.getId(), is(payment.getId()));
    assertThat(paymentPayload.getPurchaseId(), is(payment.getPurchaseId()));
    assertThat(paymentPayload.getUserId(), is(payment.getUserId()));
    assertThat(paymentPayload.getProductId(), is(payment.getProductId()));
    assertThat(paymentPayload.getPrice(), is(payment.getPrice()));
    assertThat(paymentPayload.getStatus().name(), is(payment.getStatus().name()));
    assertThat(paymentPayload.getCreatedAt(), is(payment.getCreatedAt()));
  }

  @Test
  public void shouldGetPayment() throws Exception {
    //Given
    final Long id = 1L;
    final Payment payment = PaymentMother.createPayment();
    when(this.paymentUseCase.getPayment(id)).thenReturn(Uni.createFrom().item(payment));

    //When
    final Uni<PaymentPayload> result = this.paymentResource.getPayment(id);

    //Then
    assertThat(result, is(notNullValue()));
    final PaymentPayload paymentPayload = result.subscribeAsCompletionStage().get();
    assertThat(paymentPayload, is(notNullValue()));
    assertThat(paymentPayload.getId(), is(payment.getId()));
    assertThat(paymentPayload.getPurchaseId(), is(payment.getPurchaseId()));
    assertThat(paymentPayload.getUserId(), is(payment.getUserId()));
    assertThat(paymentPayload.getProductId(), is(payment.getProductId()));
    assertThat(paymentPayload.getPrice(), is(payment.getPrice()));
    assertThat(paymentPayload.getStatus().name(), is(payment.getStatus().name()));
    assertThat(paymentPayload.getCreatedAt(), is(payment.getCreatedAt()));
  }

  @Test
  public void shouldGetErrorPayment() throws Exception {
    //Given
    final Long id = 1L;
    when(this.paymentUseCase.getPayment(id)).thenReturn(Uni.createFrom().nullItem());

    //When
    final UniAssertSubscriber<PaymentPayload> result =
        this.paymentResource.getPayment(id).subscribe().withSubscriber(UniAssertSubscriber.create());

    //Then
    assertThat(result, is(notNullValue()));
    result.awaitFailure();
    result.assertFailedWith(NotFoundException.class);
  }

  @Test
  public void shouldCreatedPayment() throws Exception {
    //Given
    final PaymentPayload paymentPayload = PaymentPayload.builder().productId("1").userId("2").price(1L).build();
    final Payment payment = Payment.builder().productId("1").userId("2").price(1L).build();
    final UriInfo uriInfo = mock(UriInfo.class);
    final UriBuilder uriBuilder = new UriBuilderImpl();
    final Long uuid = 1L;
    when(this.paymentUseCase.create(eq(payment))).thenReturn(Uni.createFrom().item(payment.toBuilder().id(1L).build()));
    when(uriInfo.getRequestUriBuilder()).thenReturn(uriBuilder);

    //When
    final Uni<Response> result = this.paymentResource.create(paymentPayload, uriInfo);

    //Then
    assertThat(result, is(notNullValue()));
    final Response response = result.subscribeAsCompletionStage().get();
    assertThat(response.getLocation(), is(notNullValue()));
    assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));
    assertThat(response.getLocation(), is(notNullValue()));
    assertThat(response.getLocation(), is(new UriBuilderImpl().path(uuid.toString()).build()));
  }

  @Test
  public void shouldCreatedPaymentError() throws Exception {
    //Given
    final PaymentPayload paymentPayload = PaymentPayload.builder().productId("1").userId("2").price(1L).build();
    final Payment payment = Payment.builder().productId("1").userId("2").price(1L).build();
    final UriInfo uriInfo = mock(UriInfo.class);
    final UriBuilder uriBuilder = new UriBuilderImpl();
    when(this.paymentUseCase.create(payment)).thenReturn(Uni.createFrom().failure(RuntimeException::new));
    when(uriInfo.getRequestUriBuilder()).thenReturn(uriBuilder);

    //When
    final Uni<Response> result = this.paymentResource.create(paymentPayload, uriInfo);

    //Then
    assertThat(result, is(notNullValue()));
    final Response response = result.subscribeAsCompletionStage().get();
    assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void shouldUpdatePayment() throws Exception {
    //Given
    final PaymentPayload paymentPayload = PaymentPayload.builder().productId("1").userId("2").price(1L).build();
    final Payment payment = Payment.builder().productId("1").userId("2").price(1L).build();
    final UriInfo uriInfo = mock(UriInfo.class);
    final UriBuilder uriBuilder = new UriBuilderImpl();
    final Long id = 1L;
    when(this.paymentUseCase.update(id, payment)).thenReturn(Uni.createFrom().item(payment.toBuilder().id(1L).build()));
    when(uriInfo.getRequestUriBuilder()).thenReturn(uriBuilder);

    //When
    final Uni<Response> result = this.paymentResource.update(id, paymentPayload, uriInfo);

    //Then
    assertThat(result, is(notNullValue()));
    final Response response = result.subscribeAsCompletionStage().get();
    assertThat(response.getLocation(), is(notNullValue()));
    assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));
    assertThat(response.getLocation(), is(notNullValue()));
    assertThat(response.getLocation(), is(new UriBuilderImpl().build()));
  }

  @Test
  public void shouldUpdatedPaymentError() throws Exception {
    //Given
    final PaymentPayload paymentPayload = PaymentPayload.builder().productId("1").userId("2").price(1L).build();
    final Payment payment = Payment.builder().productId("1").userId("2").price(1L).build();
    final UriInfo uriInfo = mock(UriInfo.class);
    final UriBuilder uriBuilder = new UriBuilderImpl();
    final Long id = 1L;
    when(this.paymentUseCase.update(id, payment)).thenReturn(Uni.createFrom().failure(RuntimeException::new));
    when(uriInfo.getRequestUriBuilder()).thenReturn(uriBuilder);

    //When
    final Uni<Response> result = this.paymentResource.update(id, paymentPayload, uriInfo);

    //Then
    assertThat(result, is(notNullValue()));
    final Response response = result.subscribeAsCompletionStage().get();
    assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
  }
}