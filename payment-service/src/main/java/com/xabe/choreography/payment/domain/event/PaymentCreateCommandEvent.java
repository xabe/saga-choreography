package com.xabe.choreography.payment.domain.event;

import com.xabe.choreography.common.infrastructure.Event;
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
public class PaymentCreateCommandEvent implements Event {

  String purchaseId;

  String userId;

  String productId;

  Long price;

  Instant sentAt;

}
