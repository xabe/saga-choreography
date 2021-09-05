package com.xabe.orchestration.shipping.infrastructure.persistence;

import com.xabe.orchestration.shipping.domain.entity.Shipping;
import com.xabe.orchestration.shipping.domain.repository.ShippingRepository;
import com.xabe.orchestration.shipping.infrastructure.persistence.dto.ShippingDTO;
import com.xabe.orchestration.shipping.infrastructure.persistence.mapper.PersistenceMapper;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

@ApplicationScoped
@RequiredArgsConstructor
public class ShippingRepositoryImpl implements ShippingRepository {

  private final Logger logger;

  private final PersistenceMapper persistenceMapper;

  private final ShippingRepositoryPanache shippingRepositoryPanache;

  @Override
  public Uni<Shipping> getShipping(final Long id) {
    this.logger.debug("Get shipping {}", id);
    return this.shippingRepositoryPanache.findById(id).map(this.persistenceMapper::toEntity);
  }

  @Override
  public Uni<List<Shipping>> getShipments() {
    this.logger.debug("Get shipments");
    return this.shippingRepositoryPanache.listAll().map(this.persistenceMapper::toEntities);
  }

  @Override
  public Uni<Shipping> create(final Shipping shipping) {
    this.logger.debug("Create shipping {}", shipping);
    return this.shippingRepositoryPanache.persistAndFlush(this.persistenceMapper.toDTO(shipping)).map(this.persistenceMapper::toEntity);
  }

  @Override
  public Uni<Shipping> update(final Long id, final Shipping shipping) {
    return this.shippingRepositoryPanache.findById(id).flatMap(this.updateShipping(id, this.persistenceMapper.toDTO(shipping)))
        .map(this.persistenceMapper::toEntity);
  }

  private Function<ShippingDTO, Uni<? extends ShippingDTO>> updateShipping(final Long id, final ShippingDTO newShippingDTO) {
    return shippingDTO -> {
      if (Objects.isNull(shippingDTO)) {
        this.logger.debug("Update: create Shipping with id {} {}", id, newShippingDTO);
        return this.shippingRepositoryPanache.persistAndFlush(newShippingDTO);
      } else {
        newShippingDTO.getPrice().ifPresent(shippingDTO::setPrice);
        shippingDTO.setStatus(newShippingDTO.getStatus());
        shippingDTO.setProductId(newShippingDTO.getProductId());
        shippingDTO.setPurchaseId(newShippingDTO.getPurchaseId());
        shippingDTO.setUserId(newShippingDTO.getUserId());
        this.logger.debug("Update: update Shipping with id {} {}", id, shippingDTO);
        return this.shippingRepositoryPanache.persistAndFlush(shippingDTO);
      }
    };
  }
}