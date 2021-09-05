package com.xabe.orchestration.order.infrastructure.application;

import com.xabe.orchestration.order.domain.entity.OrderAggregate;
import io.smallrye.mutiny.Uni;
import java.util.List;

public interface OrderUseCase {

  Uni<List<OrderAggregate>> getOrders();

  Uni<OrderAggregate> getOrder(String id);

  Uni<OrderAggregate> create(OrderAggregate orderAggregate);
}
