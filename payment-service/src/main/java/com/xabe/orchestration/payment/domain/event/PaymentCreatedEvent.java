package com.xabe.orchestration.payment.domain.event;

import com.xabe.orchestation.common.infrastructure.Event;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class PaymentCreatedEvent implements Event {

  Long id;

  String purchaseId;

  String userId;

  String productId;

  Instant createdAt;

  Long price;

  String status;

  String operationStatus;

}
