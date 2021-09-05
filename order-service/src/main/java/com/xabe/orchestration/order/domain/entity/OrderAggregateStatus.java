package com.xabe.orchestration.order.domain.entity;

public enum OrderAggregateStatus {
  START_SAGA,
  ORDER_CREATED,
  ORDER_CANCELED,
  PAYMENT_PROCESSED,
  PAYMENT_CANCELED,
  SHIPPING_SENT,
  CANCELED,
  SUCCESS;
}
