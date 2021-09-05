package com.xabe.choreography.order.domain.entity;

import com.xabe.choreography.common.infrastructure.AggregateRoot;
import com.xabe.choreography.order.domain.entity.order.Order;
import com.xabe.choreography.order.domain.entity.payment.Payment;
import com.xabe.choreography.order.domain.entity.shipping.Shipping;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class OrderAggregate implements AggregateRoot<String> {

  private String id;

  private String userId;

  private String productId;

  private Long price;

  private Order order;

  private Payment payment;

  private Shipping shipping;

  private OffsetDateTime createdAt;

  @Builder.Default
  private OrderAggregateStatus status = OrderAggregateStatus.START_SAGA;

}
