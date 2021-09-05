package com.xabe.orchestration.shipping.infrastructure.persistence.dto;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Optional;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Cacheable
public class ShippingDTO implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String purchaseId;

  private String userId;

  private String productId;

  private Long price;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private ShippingStatusDTO status = ShippingStatusDTO.ACCEPTED;

  @CreationTimestamp
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  public OffsetDateTime updatedAt;

  public Optional<Long> getPrice() {
    return Optional.ofNullable(this.price);
  }
}