package com.xabe.orchestration.shipping.infrastructure.messaging.mapper;

import com.xabe.avro.v1.ShippingCreateCommand;
import com.xabe.orchestration.shipping.domain.entity.Shipping;
import com.xabe.orchestration.shipping.domain.event.ShippingCreateCommandEvent;
import com.xabe.orchestration.shipping.domain.event.ShippingCreatedEvent;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "cdi")
public interface MessagingMapper {

  ShippingCreateCommandEvent toAvroCreateCommandEvent(ShippingCreateCommand orderCreateCommand);

  @Mapping(source = "sentAt", target = "createdAt")
  @Mapping(target = "status", expression = "java(com.xabe.orchestration.shipping.domain.entity.ShippingStatus.ACCEPTED)")
  Shipping toEntity(ShippingCreateCommandEvent paymentCreateCommandEvent);

  ShippingCreatedEvent toCreatedEvent(Shipping order, String operationStatus);

  com.xabe.avro.v1.Shipping toAvroEvent(ShippingCreatedEvent orderCreatedEvent);
  
  default Instant map(final OffsetDateTime value) {
    return Objects.isNull(value) ? Instant.now() : value.toInstant();
  }

  default OffsetDateTime map(final Instant value) {
    return Objects.isNull(value) ? OffsetDateTime.now() : value.atOffset(ZoneOffset.UTC);
  }

}
