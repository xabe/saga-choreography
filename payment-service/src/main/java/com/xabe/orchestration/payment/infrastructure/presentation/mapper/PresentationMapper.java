package com.xabe.orchestration.payment.infrastructure.presentation.mapper;

import com.xabe.orchestration.payment.domain.entity.Payment;
import com.xabe.orchestration.payment.infrastructure.presentation.payload.PaymentPayload;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "cdi")
public interface PresentationMapper {

  List<PaymentPayload> toPayloads(final List<Payment> payments);

  PaymentPayload toPayload(Payment payment);

  Payment toEntity(PaymentPayload payload);

}
