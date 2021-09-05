package com.xabe.choreography.order.infrastructure.messaging.publisher.mapper;

import com.xabe.avro.v1.PaymentCreateCommand;
import com.xabe.choreography.order.domain.event.payment.PaymentCreateCommandEvent;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "cdi")
public interface MessagingPublisherMapper {

  PaymentCreateCommand toAvroCommandEvent(PaymentCreateCommandEvent paymentCreateCommandEvent);

  default Instant map(final OffsetDateTime value) {
    return Objects.isNull(value) ? Instant.now() : value.toInstant();
  }
}
