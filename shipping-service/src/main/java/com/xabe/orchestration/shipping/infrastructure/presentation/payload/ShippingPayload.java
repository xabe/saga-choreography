package com.xabe.orchestration.shipping.infrastructure.presentation.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.OffsetDateTime;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;

@EqualsAndHashCode
@ToString
@Builder(toBuilder = true)
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
@AllArgsConstructor
//@Jacksonized
@JsonDeserialize(builder = ShippingPayload.ShippingPayloadBuilder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShippingPayload {

  Long id;

  @NotNull
  String purchaseId;

  @NotNull
  String userId;

  @NotNull
  String productId;

  @NotNull
  Long price;

  ShippingStatusPayload status;

  OffsetDateTime createdAt;

  @JsonPOJOBuilder(withPrefix = "")
  public static final class ShippingPayloadBuilder {

  }

}
