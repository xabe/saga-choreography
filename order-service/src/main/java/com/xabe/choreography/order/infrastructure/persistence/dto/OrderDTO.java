package com.xabe.choreography.order.infrastructure.persistence.dto;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Optional;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Builder(toBuilder = true)
@ToString
@NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Entity
@Table(indexes = @Index(columnList = "purchaseId"))
public class OrderDTO implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String purchaseId;

  private String userId;

  private String productId;

  private Long price;

  @CreationTimestamp
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  public OffsetDateTime updatedAt;

  @Enumerated(EnumType.STRING)
  private OrderAggregateStatusDTO status;

  @Enumerated(EnumType.STRING)
  private OrderStatusDTO orderStatus;

  private Long paymentId;

  @Enumerated(EnumType.STRING)
  private PaymentStatusDTO paymentStatus;

  private OffsetDateTime paymentCreatedAt;

  private Long shippingId;

  @Enumerated(EnumType.STRING)
  private ShippingStatusDTO shippingStatus;

  private OffsetDateTime shippingCreatedAt;

  public Optional<Long> getPrice() {
    return Optional.ofNullable(this.price);
  }
}