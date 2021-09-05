package com.xabe.orchestration.shipping.infrastructure.application;

import com.xabe.orchestration.shipping.domain.entity.Shipping;
import com.xabe.orchestration.shipping.domain.repository.ShippingRepository;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.smallrye.mutiny.Uni;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ShippingUseCaseImpl implements ShippingUseCase {

  private final ShippingRepository shippingRepository;

  @Override
  public Uni<List<Shipping>> getShipments() {
    return this.shippingRepository.getShipments();
  }

  @Override
  public Uni<Shipping> getShipping(final Long id) {
    return this.shippingRepository.getShipping(id);
  }

  @Override
  @ReactiveTransactional
  public Uni<Shipping> create(final Shipping shipping) {
    return this.shippingRepository.create(shipping);
  }

  @Override
  @ReactiveTransactional
  public Uni<Shipping> update(final Long id, final Shipping shipping) {
    return this.shippingRepository.update(id, shipping);
  }
}
