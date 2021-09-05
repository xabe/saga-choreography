package com.xabe.orchestration.order.infrastructure.persistence.mapper;

import com.xabe.orchestration.order.domain.entity.OrderAggregate;
import com.xabe.orchestration.order.infrastructure.persistence.dto.OrderDTO;
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

  List<OrderAggregate> toOrdersEntity(List<OrderDTO> orderDTOS);

  @Mapping(source = "price", target = "price", qualifiedByName = "unwrap")
  @Mapping(source = "purchaseId", target = "id")
  @Mapping(source = "id", target = "order.id")
  @Mapping(source = "orderStatus", target = "order.status")
  @Mapping(source = "createdAt", target = "order.createdAt")
  @Mapping(source = "paymentId", target = "payment.id")
  @Mapping(source = "paymentStatus", target = "payment.status")
  @Mapping(source = "paymentCreatedAt", target = "payment.createdAt")
  @Mapping(source = "shippingId", target = "shipping.id")
  @Mapping(source = "shippingStatus", target = "shipping.status")
  @Mapping(source = "shippingCreatedAt", target = "shipping.createdAt")
  OrderAggregate toEntity(OrderDTO orderDTO);

  @Mapping(source = "id", target = "purchaseId")
  @Mapping(source = "order.id", target = "id")
  @Mapping(source = "order.status", target = "orderStatus")
  @Mapping(source = "payment.id", target = "paymentId")
  @Mapping(source = "payment.status", target = "paymentStatus")
  @Mapping(source = "payment.createdAt", target = "paymentCreatedAt")
  @Mapping(source = "shipping.id", target = "shippingId")
  @Mapping(source = "shipping.status", target = "shippingStatus")
  @Mapping(source = "shipping.createdAt", target = "shippingCreatedAt")
  OrderDTO toDTO(OrderAggregate orderAggregate);

  @Named("unwrap")
  default <T> T unwrap(final Optional<T> optional) {
    return optional.orElse(null);
  }
}
