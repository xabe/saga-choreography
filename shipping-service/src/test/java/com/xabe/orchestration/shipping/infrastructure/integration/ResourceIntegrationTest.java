package com.xabe.orchestration.shipping.infrastructure.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.GsonBuilder;
import com.xabe.orchestration.shipping.infrastructure.presentation.payload.ShippingPayload;
import com.xabe.orchestration.shipping.infrastructure.presentation.payload.ShippingStatusPayload;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.gson.GsonObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@Tag("integration")
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class ResourceIntegrationTest {

  public static final String USER_ID = "1";

  public static final String PRODUCT_ID = "1";

  public static final String PURCHASE_ID = "1111";

  public static final Long PRICE = 100L;

  private final int serverPort = 8003;

  private String url;

  private ShippingPayload shippingPayload;

  @BeforeAll
  public static void init() throws IOException {
    Unirest.config().setObjectMapper(new GsonObjectMapper(Converters.registerAll(new GsonBuilder()).create()));
  }

  @Test
  @Order(1)
  public void shouldCreateShipping() throws Exception {
    //Given
    final ShippingPayload shippingPayload =
        ShippingPayload.builder().productId(PRODUCT_ID).userId(USER_ID).purchaseId(PURCHASE_ID).price(PRICE).build();

    //When
    final HttpResponse<JsonNode> response = Unirest.post(String.format("http://localhost:%d/api/shipments", this.serverPort))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).body(shippingPayload).asJson();

    //Then
    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(201));
    final List<String> locations = response.getHeaders().get(HttpHeaders.LOCATION);
    assertThat(locations, is(notNullValue()));
    assertThat(locations, is(hasSize(1)));
    this.url = locations.get(0);
  }

  @Test
  @Order(2)
  public void shouldGetShipping() throws Exception {
    //Given

    //When
    final HttpResponse<ShippingPayload> response = Unirest.get(this.url)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).asObject(ShippingPayload.class);

    //Then
    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(200));
    assertThat(response.getBody(), is(notNullValue()));
    this.shippingPayload = response.getBody();
    assertThat(this.shippingPayload.getId(), is(notNullValue()));
    assertThat(this.shippingPayload.getPurchaseId(), is(PURCHASE_ID));
    assertThat(this.shippingPayload.getProductId(), is(PRODUCT_ID));
    assertThat(this.shippingPayload.getUserId(), is(USER_ID));
    assertThat(this.shippingPayload.getPrice(), is(PRICE));
    assertThat(this.shippingPayload.getStatus().name(), is("ACCEPTED"));
    assertThat(this.shippingPayload.getCreatedAt(), is(notNullValue()));
  }

  @Test
  @Order(3)
  public void shouldGetShipments() {

    final HttpResponse<ShippingPayload[]> response = Unirest.get(String.format("http://localhost:%d/api/shipments", this.serverPort))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).asObject(ShippingPayload[].class);

    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(200));
    assertThat(response.getBody().length, is(greaterThanOrEqualTo(1)));
  }

  @Test
  @Order(4)
  public void givenAShippingPayloadWhenInvokePutThenReturnUpdatePayload() throws Exception {
    //Given
    final ShippingPayload payload = this.shippingPayload.toBuilder().status(ShippingStatusPayload.CANCELED).build();

    //When
    final HttpResponse<JsonNode> response = Unirest.put(this.url)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).body(payload).asJson();

    //Then
    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(204));
    final List<String> locations = response.getHeaders().get(HttpHeaders.LOCATION);
    assertThat(locations, is(notNullValue()));
    assertThat(locations, is(hasSize(1)));
    this.url = locations.get(0);
  }

}
