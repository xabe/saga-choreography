package com.xabe.orchestration.payment.infrastructure.integration;

import static com.xabe.orchestration.payment.infrastructure.presentation.payload.PaymentStatusPayload.CANCELED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.GsonBuilder;
import com.xabe.orchestration.payment.infrastructure.presentation.payload.PaymentPayload;
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

  private final int serverPort = 8002;

  private String url;

  private PaymentPayload paymentPayload;

  @BeforeAll
  public static void init() throws IOException {
    Unirest.config().setObjectMapper(new GsonObjectMapper(Converters.registerAll(new GsonBuilder()).create()));
  }

  @Test
  @Order(1)
  public void shouldCreatePayment() throws Exception {
    //Given
    final PaymentPayload paymentPayload =
        PaymentPayload.builder().productId(PRODUCT_ID).userId(USER_ID).purchaseId(PURCHASE_ID).price(PRICE).build();

    //When
    final HttpResponse<JsonNode> response = Unirest.post(String.format("http://localhost:%d/api/payments", this.serverPort))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).body(paymentPayload).asJson();

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
  public void shouldGetPayment() throws Exception {
    //Given

    //When
    final HttpResponse<PaymentPayload> response = Unirest.get(this.url)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).asObject(PaymentPayload.class);

    //Then
    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(200));
    assertThat(response.getBody(), is(notNullValue()));
    this.paymentPayload = response.getBody();
    assertThat(this.paymentPayload.getId(), is(notNullValue()));
    assertThat(this.paymentPayload.getPurchaseId(), is(PURCHASE_ID));
    assertThat(this.paymentPayload.getProductId(), is(PRODUCT_ID));
    assertThat(this.paymentPayload.getUserId(), is(USER_ID));
    assertThat(this.paymentPayload.getPrice(), is(PRICE));
    assertThat(this.paymentPayload.getStatus().name(), is("ACCEPTED"));
    assertThat(this.paymentPayload.getCreatedAt(), is(notNullValue()));
  }

  @Test
  @Order(3)
  public void shouldGetPayments() {

    final HttpResponse<PaymentPayload[]> response = Unirest.get(String.format("http://localhost:%d/api/payments", this.serverPort))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).asObject(PaymentPayload[].class);

    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(200));
    assertThat(response.getBody().length, is(greaterThanOrEqualTo(1)));
  }

  @Test
  @Order(4)
  public void givenAPaymentPayloadWhenInvokePutThenReturnUpdatePayload() throws Exception {
    //Given
    final PaymentPayload payload = this.paymentPayload.toBuilder().status(CANCELED).build();

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
