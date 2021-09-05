package com.xabe.orchestration.order.infrastructure;

import com.xabe.orchestration.order.domain.entity.OrderAggregate;
import com.xabe.orchestration.order.domain.entity.OrderAggregateStatus;
import com.xabe.orchestration.order.domain.entity.order.Order;
import com.xabe.orchestration.order.domain.entity.order.OrderStatus;
import com.xabe.orchestration.order.domain.entity.payment.Payment;
import com.xabe.orchestration.order.domain.entity.payment.PaymentStatus;
import com.xabe.orchestration.order.domain.entity.shipping.Shipping;
import com.xabe.orchestration.order.domain.entity.shipping.ShippingStatus;
import com.xabe.orchestration.order.domain.event.payment.PaymentCreateCommandEvent;
import com.xabe.orchestration.order.domain.event.payment.PaymentCreatedEvent;
import com.xabe.orchestration.order.domain.event.shipping.ShippingCreatedEvent;
import com.xabe.orchestration.order.infrastructure.persistence.dto.OrderAggregateStatusDTO;
import com.xabe.orchestration.order.infrastructure.persistence.dto.OrderDTO;
import com.xabe.orchestration.order.infrastructure.persistence.dto.OrderStatusDTO;
import com.xabe.orchestration.order.infrastructure.persistence.dto.PaymentStatusDTO;
import com.xabe.orchestration.order.infrastructure.persistence.dto.ShippingStatusDTO;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class OrderMother {

  private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(2021, 8, 1, 00, 00, 00, 00, ZoneOffset.UTC);

  public static OrderDTO createOrderDTO() {
    return OrderDTO.builder()
        .id(1L)
        .purchaseId("612cb1ac04e7df1b34068c21")
        .userId("2")
        .productId("3")
        .price(10L)
        .status(OrderAggregateStatusDTO.ORDER_CREATED)
        .orderStatus(OrderStatusDTO.CREATED)
        .paymentId(1L)
        .paymentStatus(PaymentStatusDTO.ACCEPTED)
        .paymentCreatedAt(OFFSET_DATE_TIME)
        .shippingId(1L)
        .shippingStatus(ShippingStatusDTO.ACCEPTED)
        .shippingCreatedAt(OFFSET_DATE_TIME)
        .createdAt(OFFSET_DATE_TIME).build();
  }

  public static PaymentCreateCommandEvent createPaymentCreateCommandEvent() {
    return PaymentCreateCommandEvent.builder()
        .purchaseId("612cb1ac04e7df1b34068c21")
        .userId("2")
        .productId("3")
        .price(1L)
        .sentAt(Instant.now())
        .build();
  }

  public static OrderAggregate createOrderAggregate() {
    return OrderAggregate.builder()
        .id("612cb1ac04e7df1b34068c21")
        .userId("userId")
        .productId("productId")
        .price(100L)
        .order(createOrder())
        .payment(createPayment())
        .shipping(createShipping())
        .status(OrderAggregateStatus.START_SAGA)
        .createdAt(OFFSET_DATE_TIME)
        .build();
  }

  private static Shipping createShipping() {
    return Shipping.builder()
        .id(1L)
        .status(ShippingStatus.ACCEPTED)
        .createdAt(OFFSET_DATE_TIME)
        .build();
  }

  private static Payment createPayment() {
    return Payment.builder()
        .id(1L)
        .status(PaymentStatus.ACCEPTED)
        .createdAt(OFFSET_DATE_TIME)
        .build();
  }

  private static Order createOrder() {
    return Order.builder()
        .id(1L)
        .status(OrderStatus.CREATED)
        .createdAt(OFFSET_DATE_TIME)
        .build();
  }

  public static PaymentCreatedEvent createPaymentCreatedEvent(final String operationStatus) {
    return PaymentCreatedEvent.builder()
        .id(1L)
        .userId("1")
        .productId("2")
        .purchaseId("3")
        .price(10L)
        .operationStatus(operationStatus)
        .status("ACCEPTED")
        .createdAt(OFFSET_DATE_TIME.toInstant())
        .build();
  }

  public static ShippingCreatedEvent createShippingCreatedEvent(final String operationStatus) {
    return ShippingCreatedEvent.builder()
        .id(1L)
        .userId("1")
        .productId("2")
        .purchaseId("3")
        .price(10L)
        .operationStatus(operationStatus)
        .status("ACCEPTED")
        .createdAt(OFFSET_DATE_TIME.toInstant())
        .build();
  }
}
