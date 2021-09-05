package com.xabe.orchestration.payment.infrastructure.persistence.mapper;

import com.xabe.orchestration.payment.infrastructure.persistence.dto.PaymentDTO;
import com.xabe.orchestration.payment.domain.entity.Payment;
import java.util.List;
import java.util.Optional;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "cdi")
public interface PersistenceMapper {

  @Mapping(source = "price", target = "price", qualifiedByName = "unwrap")
  Payment toEntity(PaymentDTO paymentDTO);

  List<Payment> toEntities(List<PaymentDTO> paymentDTOS);

  @Mapping(target = "id", ignore = true)
  PaymentDTO toDTO(Payment payment);

  @Named("unwrap")
  default <T> T unwrap(final Optional<T> optional) {
    return optional.orElse(null);
  }
}
