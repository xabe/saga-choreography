package com.xabe.orchestration.payment.infrastructure.persistence;

import com.xabe.orchestration.payment.domain.repository.PaymentRepository;
import com.xabe.orchestration.payment.domain.entity.Payment;
import com.xabe.orchestration.payment.infrastructure.persistence.dto.PaymentDTO;
import com.xabe.orchestration.payment.infrastructure.persistence.mapper.PersistenceMapper;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

@ApplicationScoped
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

  private final Logger logger;

  private final PersistenceMapper persistenceMapper;

  private final PaymentRepositoryPanache paymentRepositoryPanache;

  @Override
  public Uni<Payment> getPayment(final Long id) {
    this.logger.debug("Get payment {}", id);
    return this.paymentRepositoryPanache.findById(id).map(this.persistenceMapper::toEntity);
  }

  @Override
  public Uni<List<Payment>> getPayments() {
    this.logger.debug("Get payments");
    return this.paymentRepositoryPanache.listAll().map(this.persistenceMapper::toEntities);
  }

  @Override
  public Uni<Payment> create(final Payment payment) {
    this.logger.debug("Create payment {}", payment);
    return this.paymentRepositoryPanache.persistAndFlush(this.persistenceMapper.toDTO(payment)).map(this.persistenceMapper::toEntity);
  }

  @Override
  public Uni<Payment> update(final Long id, final Payment payment) {
    return this.paymentRepositoryPanache.findById(id).flatMap(this.updatePayment(id, this.persistenceMapper.toDTO(payment)))
        .map(this.persistenceMapper::toEntity);
  }

  private Function<PaymentDTO, Uni<? extends PaymentDTO>> updatePayment(final Long id, final PaymentDTO newPaymentDTO) {
    return paymentDTO -> {
      if (Objects.isNull(paymentDTO)) {
        this.logger.debug("Update: create Payment with id {} {}", id, newPaymentDTO);
        return this.paymentRepositoryPanache.persistAndFlush(newPaymentDTO);
      } else {
        newPaymentDTO.getPrice().ifPresent(paymentDTO::setPrice);
        paymentDTO.setStatus(newPaymentDTO.getStatus());
        paymentDTO.setProductId(newPaymentDTO.getProductId());
        paymentDTO.setPurchaseId(newPaymentDTO.getPurchaseId());
        paymentDTO.setUserId(newPaymentDTO.getUserId());
        this.logger.debug("Update: update Payment with id {} {}", id, paymentDTO);
        return this.paymentRepositoryPanache.persistAndFlush(paymentDTO);
      }
    };
  }
}