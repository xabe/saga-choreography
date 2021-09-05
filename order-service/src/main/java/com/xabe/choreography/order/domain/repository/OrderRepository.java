package com.xabe.choreography.order.domain.repository;

import com.xabe.choreography.common.infrastructure.repository.Repository;
import com.xabe.choreography.order.domain.entity.OrderAggregate;

public interface OrderRepository extends Repository<OrderAggregate, String> {

}
