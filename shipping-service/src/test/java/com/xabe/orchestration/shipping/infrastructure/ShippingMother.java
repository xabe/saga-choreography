package com.xabe.orchestration.shipping.infrastructure;

import com.xabe.orchestration.shipping.domain.entity.Shipping;
import com.xabe.orchestration.shipping.domain.entity.ShippingStatus;
import com.xabe.orchestration.shipping.domain.event.ShippingCreateCommandEvent;
import com.xabe.orchestration.shipping.domain.event.ShippingCreatedEvent;
import com.xabe.orchestration.shipping.infrastructure.persistence.dto.ShippingDTO;
import com.xabe.orchestration.shipping.infrastructure.persistence.dto.ShippingStatusDTO;
import java.time.Instant;
import java.time.OffsetDateTime;

public class ShippingMother {

  public static Shipping createShipping() {
    return Shipping.builder()
        .id(1L)
        .purchaseId("1111")
        .userId("2")
        .productId("3")
        .price(10L)
        .status(ShippingStatus.ACCEPTED)
        .createdAt(OffsetDateTime.MAX).build();
  }

  public static Shipping createShippingNew() {
    return Shipping.builder()
        .id(1L)
        .purchaseId("222")
        .userId("3")
        .productId("4")
        .price(100L)
        .status(ShippingStatus.CANCELED)
        .createdAt(OffsetDateTime.MAX).build();
  }

  public static ShippingDTO createShippingDTO() {
    return ShippingDTO.builder()
        .id(1L)
        .purchaseId("1111")
        .userId("2")
        .productId("3")
        .price(10L)
        .status(ShippingStatusDTO.ACCEPTED)
        .createdAt(OffsetDateTime.MAX).build();
  }

  public static ShippingCreatedEvent createShippingCreatedEvent() {
    return ShippingCreatedEvent.builder()
        .id(1L)
        .purchaseId("1111")
        .userId("2")
        .productId("3")
        .price(10L)
        .status("ACCEPTED")
        .operationStatus("SUCCESS")
        .createdAt(Instant.MAX).build();
  }

  public static ShippingCreateCommandEvent createPaymentCreateCommandEvent() {
    return ShippingCreateCommandEvent.builder()
        .purchaseId("1111")
        .userId("2")
        .productId("3")
        .price(1L)
        .sentAt(Instant.now())
        .build();
  }

}
