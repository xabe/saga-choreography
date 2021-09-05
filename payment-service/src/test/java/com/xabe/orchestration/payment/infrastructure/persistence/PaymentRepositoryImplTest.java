package com.xabe.orchestration.payment.infrastructure.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xabe.orchestration.payment.domain.repository.PaymentRepository;
import com.xabe.orchestration.payment.infrastructure.PaymentMother;
import com.xabe.orchestration.payment.infrastructure.persistence.dto.PaymentDTO;
import com.xabe.orchestration.payment.domain.entity.Payment;
import com.xabe.orchestration.payment.infrastructure.persistence.mapper.PersistenceMapperImpl;
import io.smallrye.mutiny.Uni;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

class PaymentRepositoryImplTest {

  private PaymentRepositoryPanache paymentRepositoryPanache;

  private PaymentRepository paymentRepository;

  @BeforeEach
  public void setUp() throws Exception {
    final Logger logger = mock(Logger.class);
    this.paymentRepositoryPanache = mock(PaymentRepositoryPanache.class);
    this.paymentRepository = new PaymentRepositoryImpl(logger, new PersistenceMapperImpl(), this.paymentRepositoryPanache);
  }

  @Test
  public void shouldGetPayment() throws Exception {
    //Given
    final Long id = 1L;
    final PaymentDTO paymentDTO = PaymentMother.createPaymentDTO();
    when(this.paymentRepositoryPanache.findById(id)).thenReturn(Uni.createFrom().item(paymentDTO));

    //When
    final Uni<Payment> result = this.paymentRepository.getPayment(id);

    //Then
    assertThat(result, is(notNullValue()));
    final Payment payment = result.subscribeAsCompletionStage().get();
    assertThat(payment.getId(), is(paymentDTO.getId()));
    assertThat(payment.getPurchaseId(), is(paymentDTO.getPurchaseId()));
    assertThat(payment.getUserId(), is(paymentDTO.getUserId()));
    assertThat(payment.getProductId(), is(paymentDTO.getProductId()));
    assertThat(payment.getPrice(), is(paymentDTO.getPrice().get()));
    assertThat(payment.getStatus().name(), is(paymentDTO.getStatus().name()));
    assertThat(payment.getCreatedAt(), is(paymentDTO.getCreatedAt()));
  }

  @Test
  public void shouldGetAllPayments() throws Exception {
    //Given
    final PaymentDTO paymentDTO = PaymentMother.createPaymentDTO();
    when(this.paymentRepositoryPanache.listAll()).thenReturn(Uni.createFrom().item(List.of(paymentDTO)));

    //When
    final Uni<List<Payment>> result = this.paymentRepository.getPayments();

    //Then
    assertThat(result, is(notNullValue()));
    final List<Payment> payments = result.subscribeAsCompletionStage().get();
    assertThat(payments, is(notNullValue()));
    assertThat(payments, is(hasSize(1)));
    final Payment payment = payments.get(0);
    assertThat(payment.getId(), is(paymentDTO.getId()));
    assertThat(payment.getPurchaseId(), is(paymentDTO.getPurchaseId()));
    assertThat(payment.getUserId(), is(paymentDTO.getUserId()));
    assertThat(payment.getProductId(), is(paymentDTO.getProductId()));
    assertThat(payment.getPrice(), is(paymentDTO.getPrice().get()));
    assertThat(payment.getStatus().name(), is(paymentDTO.getStatus().name()));
    assertThat(payment.getCreatedAt(), is(paymentDTO.getCreatedAt()));
  }

  @Test
  public void shouldCreatePayment() throws Exception {
    //Given
    final Payment payment = PaymentMother.createPayment();
    final PaymentDTO paymentDTO = PaymentMother.createPaymentDTO();
    final ArgumentCaptor<PaymentDTO> argumentCaptor = ArgumentCaptor.forClass(PaymentDTO.class);
    when(this.paymentRepositoryPanache.persistAndFlush(argumentCaptor.capture())).thenReturn(Uni.createFrom().item(paymentDTO));

    //When
    final Uni<Payment> result = this.paymentRepository.create(payment);

    //Then
    assertThat(result, is(notNullValue()));
    final Payment paymentResult = result.subscribeAsCompletionStage().get();
    assertThat(paymentResult.getId(), is(paymentDTO.getId()));
    assertThat(paymentResult.getPurchaseId(), is(paymentDTO.getPurchaseId()));
    assertThat(paymentResult.getUserId(), is(paymentDTO.getUserId()));
    assertThat(paymentResult.getProductId(), is(paymentDTO.getProductId()));
    assertThat(paymentResult.getPrice(), is(paymentDTO.getPrice().get()));
    assertThat(paymentResult.getStatus().name(), is(paymentDTO.getStatus().name()));
    assertThat(paymentResult.getCreatedAt(), is(paymentDTO.getCreatedAt()));
    final PaymentDTO value = argumentCaptor.getValue();
    assertThat(value.getId(), is(nullValue()));
    assertThat(value.getPurchaseId(), is(payment.getPurchaseId()));
    assertThat(value.getUserId(), is(payment.getUserId()));
    assertThat(value.getProductId(), is(payment.getProductId()));
    assertThat(value.getPrice().get(), is(payment.getPrice()));
    assertThat(value.getStatus().name(), is(payment.getStatus().name()));
    assertThat(value.getCreatedAt(), is(payment.getCreatedAt()));
  }

  @Test
  public void shouldUpdatePaymentNew() throws Exception {
    //Given
    final Long id = 1L;
    final Payment payment = PaymentMother.createPayment();
    final PaymentDTO paymentDTO = PaymentMother.createPaymentDTO();
    final ArgumentCaptor<PaymentDTO> argumentCaptor = ArgumentCaptor.forClass(PaymentDTO.class);
    when(this.paymentRepositoryPanache.findById(id)).thenReturn(Uni.createFrom().nullItem());
    when(this.paymentRepositoryPanache.persistAndFlush(argumentCaptor.capture())).thenReturn(Uni.createFrom().item(paymentDTO));

    //When
    final Uni<Payment> result = this.paymentRepository.update(id, payment);

    //Then
    assertThat(result, is(notNullValue()));
    final Payment paymentResult = result.subscribeAsCompletionStage().get();
    assertThat(paymentResult.getId(), is(paymentDTO.getId()));
    assertThat(paymentResult.getPurchaseId(), is(paymentDTO.getPurchaseId()));
    assertThat(paymentResult.getUserId(), is(paymentDTO.getUserId()));
    assertThat(paymentResult.getProductId(), is(paymentDTO.getProductId()));
    assertThat(paymentResult.getPrice(), is(paymentDTO.getPrice().get()));
    assertThat(paymentResult.getStatus().name(), is(paymentDTO.getStatus().name()));
    assertThat(paymentResult.getCreatedAt(), is(paymentDTO.getCreatedAt()));
    final PaymentDTO value = argumentCaptor.getValue();
    assertThat(value.getId(), is(nullValue()));
    assertThat(value.getPurchaseId(), is(payment.getPurchaseId()));
    assertThat(value.getUserId(), is(payment.getUserId()));
    assertThat(value.getProductId(), is(payment.getProductId()));
    assertThat(value.getPrice().get(), is(payment.getPrice()));
    assertThat(value.getStatus().name(), is(payment.getStatus().name()));
    assertThat(value.getCreatedAt(), is(payment.getCreatedAt()));
  }

  @Test
  public void shouldUpdatePaymentOld() throws Exception {
    //Given
    final Long id = 1L;
    final Payment payment = PaymentMother.createPaymentNew();
    final PaymentDTO paymentDTO = PaymentMother.createPaymentDTO();
    final ArgumentCaptor<PaymentDTO> argumentCaptor = ArgumentCaptor.forClass(PaymentDTO.class);
    when(this.paymentRepositoryPanache.findById(id)).thenReturn(Uni.createFrom().item(paymentDTO));
    when(this.paymentRepositoryPanache.persistAndFlush(argumentCaptor.capture())).thenReturn(Uni.createFrom().item(paymentDTO));

    //When
    final Uni<Payment> result = this.paymentRepository.update(id, payment);

    //Then
    assertThat(result, is(notNullValue()));
    final Payment paymentResult = result.subscribeAsCompletionStage().get();
    assertThat(paymentResult.getId(), is(paymentDTO.getId()));
    assertThat(paymentResult.getPurchaseId(), is(paymentDTO.getPurchaseId()));
    assertThat(paymentResult.getUserId(), is(paymentDTO.getUserId()));
    assertThat(paymentResult.getProductId(), is(paymentDTO.getProductId()));
    assertThat(paymentResult.getPrice(), is(paymentDTO.getPrice().get()));
    assertThat(paymentResult.getStatus().name(), is(paymentDTO.getStatus().name()));
    assertThat(paymentResult.getCreatedAt(), is(paymentDTO.getCreatedAt()));
    final PaymentDTO value = argumentCaptor.getValue();
    assertThat(value.getId(), is(payment.getId()));
    assertThat(value.getPurchaseId(), is(payment.getPurchaseId()));
    assertThat(value.getUserId(), is(payment.getUserId()));
    assertThat(value.getProductId(), is(payment.getProductId()));
    assertThat(value.getPrice().get(), is(payment.getPrice()));
    assertThat(value.getStatus().name(), is(payment.getStatus().name()));
    assertThat(value.getCreatedAt(), is(payment.getCreatedAt()));
  }

}