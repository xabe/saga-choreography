package com.xabe.orchestration.payment.infrastructure.application;

import com.xabe.orchestration.payment.domain.entity.Payment;
import com.xabe.orchestration.payment.domain.repository.PaymentRepository;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.smallrye.mutiny.Uni;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class PaymentUseCaseImpl implements PaymentUseCase {

  private final PaymentRepository paymentRepository;

  @Override
  public Uni<List<Payment>> getPayments() {
    return this.paymentRepository.getPayments();
  }

  @Override
  public Uni<Payment> getPayment(final Long id) {
    return this.paymentRepository.getPayment(id);
  }

  @Override
  @ReactiveTransactional
  public Uni<Payment> create(final Payment payment) {
    return this.paymentRepository.create(payment);
  }

  @Override
  @ReactiveTransactional
  public Uni<Payment> update(final Long id, final Payment payment) {
    return this.paymentRepository.update(id, payment);
  }
}
