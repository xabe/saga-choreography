package com.xabe.choreography.payment.domain.repository;

import com.xabe.choreography.payment.domain.entity.Payment;
import io.smallrye.mutiny.Uni;
import java.util.List;

public interface PaymentRepository {

  Uni<Payment> getPayment(Long id);

  Uni<List<Payment>> getPayments();

  Uni<Payment> create(Payment payment);

  Uni<Payment> update(Long id, Payment payment);
}
