package com.xabe.orchestration.payment.infrastructure.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xabe.orchestration.payment.domain.repository.PaymentRepository;
import com.xabe.orchestration.payment.domain.entity.Payment;
import io.smallrye.mutiny.Uni;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentUseCaseImplTest {

  private PaymentRepository paymentRepository;

  private PaymentUseCase paymentUseCase;

  @BeforeEach
  public void setUp() throws Exception {
    this.paymentRepository = mock(PaymentRepository.class);
    this.paymentUseCase = new PaymentUseCaseImpl(this.paymentRepository);
  }

  @Test
  public void givenAIdWhenInvokeGetPaymentThenReturnPayment() throws Exception {
    //Given
    final Long id = 1L;
    when(this.paymentRepository.getPayment(id)).thenReturn(Uni.createFrom().item(Payment.builder().build()));

    //When
    final Uni<Payment> payment = this.paymentUseCase.getPayment(id);

    //Then
    assertThat(payment, is(notNullValue()));
    final Payment result = payment.subscribeAsCompletionStage().get();
    assertThat(result, is(notNullValue()));
  }

  @Test
  public void shouldGetAllPayments() throws Exception {
    //Given
    when(this.paymentRepository.getPayments()).thenReturn(Uni.createFrom().item(List.of(Payment.builder().build())));

    //When
    final Uni<List<Payment>> payments = this.paymentUseCase.getPayments();

    //Then
    assertThat(payments, is(notNullValue()));
    final List<Payment> result = payments.subscribeAsCompletionStage().get();
    assertThat(result, is(notNullValue()));
    assertThat(result, is(hasSize(1)));
  }

  @Test
  public void shouldCreatePayment() throws Exception {
    //Given
    final Payment payment = Payment.builder().build();
    when(this.paymentRepository.create(any())).thenReturn(Uni.createFrom().item(payment));

    //When
    final Uni<Payment> result = this.paymentUseCase.create(payment);

    //Then
    assertThat(result, is(notNullValue()));
    assertThat(result.subscribeAsCompletionStage().get(), is(notNullValue()));
  }

  @Test
  public void shouldUpdatePayment() throws Exception {
    //Given
    final Payment payment = Payment.builder().build();
    final Long id = 1L;
    when(this.paymentRepository.update(id, payment)).thenReturn(Uni.createFrom().item(payment));

    //When
    final Uni<Payment> result = this.paymentUseCase.update(id, payment);

    //Then
    assertThat(result, is(notNullValue()));
    assertThat(result.subscribeAsCompletionStage().get(), is(notNullValue()));
  }
}