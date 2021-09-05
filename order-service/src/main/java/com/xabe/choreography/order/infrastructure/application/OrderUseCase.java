package com.xabe.choreography.order.infrastructure.application;

import com.xabe.choreography.order.domain.entity.OrderAggregate;
import io.smallrye.mutiny.Uni;
import java.util.List;

public interface OrderUseCase {

  Uni<List<OrderAggregate>> getOrders();

  Uni<OrderAggregate> getOrder(String id);

  Uni<OrderAggregate> create(OrderAggregate orderAggregate);
}
