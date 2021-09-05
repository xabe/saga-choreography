package com.xabe.orchestration.payment.infrastructure.messaging.mapper;

import com.xabe.avro.v1.PaymentCreateCommand;
import com.xabe.orchestration.payment.domain.entity.Payment;
import com.xabe.orchestration.payment.domain.event.PaymentCreateCommandEvent;
import com.xabe.orchestration.payment.domain.event.PaymentCreatedEvent;
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

  PaymentCreateCommandEvent toAvroCreateCommandEvent(PaymentCreateCommand paymentCreateCommand);

  @Mapping(source = "sentAt", target = "createdAt")
  @Mapping(target = "status", expression = "java(com.xabe.orchestration.payment.domain.entity.PaymentStatus.ACCEPTED)")
  Payment toEntity(PaymentCreateCommandEvent paymentCreateCommandEvent);

  PaymentCreatedEvent toCreatedEvent(Payment order, String operationStatus);

  com.xabe.avro.v1.Payment toAvroEvent(PaymentCreatedEvent orderCreatedEvent);

  default Instant map(final OffsetDateTime value) {
    return Objects.isNull(value) ? Instant.now() : value.toInstant();
  }

  default OffsetDateTime map(final Instant value) {
    return Objects.isNull(value) ? OffsetDateTime.now() : value.atOffset(ZoneOffset.UTC);
  }

}
