package com.xabe.choreography.order.infrastructure.persistence;

import com.xabe.choreography.common.infrastructure.exception.EntityNotFoundException;
import com.xabe.choreography.order.domain.entity.OrderAggregate;
import com.xabe.choreography.order.domain.repository.OrderRepository;
import com.xabe.choreography.order.infrastructure.persistence.dto.OrderDTO;
import com.xabe.choreography.order.infrastructure.persistence.mapper.PersistenceMapper;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

@ApplicationScoped
@RequiredArgsConstructor
class OderRepositoryImpl implements OrderRepository {

  private final Logger logger;

  private final PersistenceMapper persistenceMapper;

  private final OrderRepositoryPanache orderRepositoryPanache;

  @Override
  public Uni<OrderAggregate> load(final String id) {
    this.logger.debug("Get Order {}", id);
    return this.orderRepositoryPanache.find("purchaseId", id).firstResult()
        .map(this.persistenceMapper::toEntity);
  }

  @Override
  public Uni<List<OrderAggregate>> getAll() {
    this.logger.debug("Get Orders");
    return this.orderRepositoryPanache.listAll().map(this.persistenceMapper::toOrdersEntity);
  }

  @Override
  public Uni<OrderAggregate> save(final OrderAggregate orderAggregate) {
    this.logger.debug("Create Order {}", orderAggregate);
    return this.orderRepositoryPanache.persistAndFlush(this.persistenceMapper.toDTO(orderAggregate)).map(this.persistenceMapper::toEntity);
  }

  @Override
  public Uni<OrderAggregate> update(final OrderAggregate orderAggregate) {
    this.logger.debug("Update Order {}", orderAggregate);
    return this.orderRepositoryPanache.find("purchaseId", orderAggregate.getId()).firstResult()
        .onItem().ifNull().failWith(() -> new EntityNotFoundException("OrderAggregate"))
        .flatMap(this.updateOrder(this.persistenceMapper.toDTO(orderAggregate)))
        .map(this.persistenceMapper::toEntity);
  }

  private Function<OrderDTO, Uni<? extends OrderDTO>> updateOrder(final OrderDTO newOrderDTO) {
    return orderDTO -> {
      newOrderDTO.getPrice().ifPresent(orderDTO::setPrice);
      orderDTO.setStatus(newOrderDTO.getStatus());
      orderDTO.setProductId(newOrderDTO.getProductId());
      orderDTO.setPurchaseId(newOrderDTO.getPurchaseId());
      orderDTO.setUserId(newOrderDTO.getUserId());
      orderDTO.setStatus(newOrderDTO.getStatus());
      orderDTO.setOrderStatus(newOrderDTO.getOrderStatus());
      orderDTO.setPaymentId(newOrderDTO.getPaymentId());
      orderDTO.setPaymentStatus(newOrderDTO.getPaymentStatus());
      orderDTO.setPaymentCreatedAt(newOrderDTO.getPaymentCreatedAt());
      orderDTO.setShippingId(newOrderDTO.getShippingId());
      orderDTO.setShippingStatus(newOrderDTO.getShippingStatus());
      orderDTO.setShippingCreatedAt(newOrderDTO.getShippingCreatedAt());
      this.logger.debug("Update: update Order with id {}", orderDTO);
      return this.orderRepositoryPanache.persistAndFlush(orderDTO);
    };
  }
}