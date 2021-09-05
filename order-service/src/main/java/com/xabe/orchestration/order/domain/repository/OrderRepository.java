package com.xabe.orchestration.order.domain.repository;

import com.xabe.orchestation.common.infrastructure.repository.Repository;
import com.xabe.orchestration.order.domain.entity.OrderAggregate;

public interface OrderRepository extends Repository<OrderAggregate, String> {

}
