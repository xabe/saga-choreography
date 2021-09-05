package com.xabe.choreography.shipping.domain.repository;

import com.xabe.choreography.shipping.domain.entity.Shipping;
import io.smallrye.mutiny.Uni;
import java.util.List;

public interface ShippingRepository {

  Uni<Shipping> getShipping(Long id);

  Uni<List<Shipping>> getShipments();

  Uni<Shipping> create(Shipping order);

  Uni<Shipping> update(Long id, Shipping shipping);
}
