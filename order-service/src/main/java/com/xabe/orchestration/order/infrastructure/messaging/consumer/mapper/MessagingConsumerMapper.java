package com.xabe.orchestration.order.infrastructure.messaging.consumer.mapper;

import com.xabe.orchestration.order.domain.entity.payment.Payment;
import com.xabe.orchestration.order.domain.entity.shipping.Shipping;
import com.xabe.orchestration.order.domain.event.payment.PaymentCreatedEvent;
import com.xabe.orchestration.order.domain.event.shipping.ShippingCreatedEvent;
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
public interface MessagingConsumerMapper {

  @Mapping(source = "payment.id", target = "id")
  @Mapping(source = "payment.purchaseId", target = "purchaseId")
  @Mapping(source = "payment.userId", target = "userId")
  @Mapping(source = "payment.productId", target = "productId")
  @Mapping(source = "payment.price", target = "price")
  @Mapping(source = "payment.status", target = "status")
  @Mapping(source = "payment.createdAt", target = "createdAt")
  PaymentCreatedEvent toAvroPaymentCreatedEvent(com.xabe.avro.v1.PaymentCreatedEvent paymentCreatedEvent);

  @Mapping(source = "shipping.id", target = "id")
  @Mapping(source = "shipping.purchaseId", target = "purchaseId")
  @Mapping(source = "shipping.userId", target = "userId")
  @Mapping(source = "shipping.productId", target = "productId")
  @Mapping(source = "shipping.price", target = "price")
  @Mapping(source = "shipping.status", target = "status")
  @Mapping(source = "shipping.createdAt", target = "createdAt")
  ShippingCreatedEvent toAvroShippingCreatedEvent(com.xabe.avro.v1.ShippingCreatedEvent shippingCreatedEvent);

  default OffsetDateTime map(final Instant value) {
    return Objects.isNull(value) ? OffsetDateTime.now() : value.atOffset(ZoneOffset.UTC);
  }

  Payment toPaymentEntity(PaymentCreatedEvent paymentCreatedEvent);

  Shipping toShippingEntity(ShippingCreatedEvent shippingCreatedEvent);
}