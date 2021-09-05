package com.xabe.orchestration.shipping.infrastructure.persistence.mapper;

import com.xabe.orchestration.shipping.domain.entity.Shipping;
import com.xabe.orchestration.shipping.infrastructure.persistence.dto.ShippingDTO;
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
  Shipping toEntity(ShippingDTO shippingDTO);

  List<Shipping> toEntities(List<ShippingDTO> shippingDTOS);

  @Mapping(target = "id", ignore = true)
  ShippingDTO toDTO(Shipping shipping);

  @Named("unwrap")
  default <T> T unwrap(final Optional<T> optional) {
    return optional.orElse(null);
  }
}
