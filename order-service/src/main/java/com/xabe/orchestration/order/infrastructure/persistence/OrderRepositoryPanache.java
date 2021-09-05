package com.xabe.orchestration.order.infrastructure.persistence;

import com.xabe.orchestration.order.infrastructure.persistence.dto.OrderDTO;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderRepositoryPanache implements PanacheRepositoryBase<OrderDTO, String> {

}
