package com.xabe.orchestration.shipping.infrastructure.application;

import com.xabe.orchestration.shipping.domain.entity.Shipping;
import io.smallrye.mutiny.Uni;
import java.util.List;

public interface ShippingUseCase {

  Uni<List<Shipping>> getShipments();

  Uni<Shipping> getShipping(Long id);

  Uni<Shipping> create(Shipping shipping);

  Uni<Shipping> update(Long id, Shipping shipping);
}
