package com.xabe.choreography.payment.infrastructure.persistence;

import com.xabe.choreography.payment.infrastructure.persistence.dto.PaymentDTO;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentRepositoryPanache implements PanacheRepositoryBase<PaymentDTO, Long> {

}
