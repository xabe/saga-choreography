package com.xabe.orchestration.shipping.infrastructure.presentation.mapper;

import com.xabe.orchestration.shipping.domain.entity.Shipping;
import com.xabe.orchestration.shipping.infrastructure.presentation.payload.ShippingPayload;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "cdi")
public interface PresentationMapper {

  List<ShippingPayload> toPayloads(final List<Shipping> shippings);

  ShippingPayload toPayload(Shipping shipping);

  Shipping toEntity(ShippingPayload payload);

}
