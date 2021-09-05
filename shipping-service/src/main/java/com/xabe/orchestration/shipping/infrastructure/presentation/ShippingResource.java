package com.xabe.orchestration.shipping.infrastructure.presentation;

import com.xabe.orchestration.shipping.domain.entity.Shipping;
import com.xabe.orchestration.shipping.infrastructure.application.ShippingUseCase;
import com.xabe.orchestration.shipping.infrastructure.presentation.mapper.PresentationMapper;
import com.xabe.orchestration.shipping.infrastructure.presentation.payload.ShippingPayload;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.function.Function;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;

@Singleton
@Path("/shipments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class ShippingResource {

  private final ShippingUseCase shippingUseCase;

  private final PresentationMapper presentationMapper;

  @GET
  public Uni<List<ShippingPayload>> getShipments() {
    return this.shippingUseCase.getShipments().map(this.presentationMapper::toPayloads);
  }

  @Path("/{id}")
  @GET
  public Uni<ShippingPayload> getShipping(@PathParam("id") final Long id) {
    return this.shippingUseCase.getShipping(id).map(this.presentationMapper::toPayload)
        .onItem().ifNull().failWith(NotFoundException::new);
  }

  @POST
  public Uni<Response> create(@Valid final ShippingPayload shippingPayload, @Context final UriInfo uriInfo) {
    return this.shippingUseCase.create(this.presentationMapper.toEntity(shippingPayload))
        .map(this.createResponseSuccessCreate(uriInfo))
        .onFailure().recoverWithItem(() -> Response.status(Response.Status.BAD_REQUEST).build());
  }

  private Function<Shipping, Response> createResponseSuccessCreate(final UriInfo uriInfo) {
    return shipping -> Response.created(uriInfo.getRequestUriBuilder().path(shipping.getId().toString()).build()).build();
  }

  @Path("/{id}")
  @PUT
  public Uni<Response> update(@PathParam("id") final Long id, @Valid final ShippingPayload shippingPayload,
      @Context final UriInfo uriInfo) {
    return this.shippingUseCase.update(id, this.presentationMapper.toEntity(shippingPayload))
        .map(order -> Response.noContent().location(uriInfo.getRequestUriBuilder().build()).build())
        .onFailure().recoverWithItem(() -> Response.status(Response.Status.BAD_REQUEST).build());
  }
}
