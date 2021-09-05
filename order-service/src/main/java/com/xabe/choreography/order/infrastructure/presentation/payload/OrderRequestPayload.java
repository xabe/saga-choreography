package com.xabe.choreography.order.infrastructure.presentation.payload;

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
@JsonDeserialize(builder = OrderRequestPayload.OrderRequestPayloadBuilder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderRequestPayload {

  @NotNull
  String userId;

  @NotNull
  String productId;

  @NotNull
  Long price;

  @Builder.Default
  OffsetDateTime createdAt = OffsetDateTime.now();

  @JsonPOJOBuilder(withPrefix = "")
  public static final class OrderRequestPayloadBuilder {

  }

}
