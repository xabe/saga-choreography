package com.xabe.choreography.order.infrastructure.presentation.mapper;

import com.xabe.choreography.order.domain.entity.OrderAggregate;
import com.xabe.choreography.order.infrastructure.presentation.payload.OrderAggregatePayload;
import com.xabe.choreography.order.infrastructure.presentation.payload.OrderRequestPayload;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "cdi")
public interface PresentationMapper {

  List<OrderAggregatePayload> toPayloads(final List<OrderAggregate> orders);

  OrderAggregatePayload toPayload(OrderAggregate order);

  OrderAggregate toEntity(OrderRequestPayload orderRequestPayload, String id);

}
