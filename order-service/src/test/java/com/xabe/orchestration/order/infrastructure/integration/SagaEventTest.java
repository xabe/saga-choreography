package com.xabe.orchestration.order.infrastructure.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.GsonBuilder;
import com.xabe.avro.v1.MessageEnvelopePayment;
import com.xabe.avro.v1.MessageEnvelopeStatus;
import com.xabe.avro.v1.Metadata;
import com.xabe.avro.v1.Payment;
import com.xabe.avro.v1.PaymentCreateCommand;
import com.xabe.avro.v1.PaymentCreatedEvent;
import com.xabe.avro.v1.PaymentOperationStatus;
import com.xabe.avro.v1.PaymentStatus;
import com.xabe.avro.v1.Shipping;
import com.xabe.avro.v1.ShippingCreatedEvent;
import com.xabe.avro.v1.ShippingOperationStatus;
import com.xabe.avro.v1.ShippingStatus;
import com.xabe.orchestation.integration.KafkaConsumer;
import com.xabe.orchestation.integration.KafkaProducer;
import com.xabe.orchestation.integration.UrlUtil;
import com.xabe.orchestration.order.infrastructure.presentation.payload.OrderAggregatePayload;
import com.xabe.orchestration.order.infrastructure.presentation.payload.OrderAggregateStatusPayload;
import com.xabe.orchestration.order.infrastructure.presentation.payload.OrderPayload;
import com.xabe.orchestration.order.infrastructure.presentation.payload.OrderRequestPayload;
import com.xabe.orchestration.order.infrastructure.presentation.payload.OrderStatusPayload;
import com.xabe.orchestration.order.infrastructure.presentation.payload.PaymentPayload;
import com.xabe.orchestration.order.infrastructure.presentation.payload.PaymentStatusPayload;
import com.xabe.orchestration.order.infrastructure.presentation.payload.ShippingPayload;
import com.xabe.orchestration.order.infrastructure.presentation.payload.ShippingStatusPayload;
import groovy.lang.Tuple2;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.gson.GsonObjectMapper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@Tag("integration")
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class SagaEventTest {

  public static final int TIMEOUT_MS = 5000;

  public static final int DELAY_MS = 2500;

  public static final int POLL_INTERVAL_MS = 500;

  public static final String USER_ID = "2";

  public static final String PRODUCT_ID = "2";

  public static final Long PRICE = 200L;

  public static final Long ID_PAYMENT = 400L;

  public static final Long ID_SHIPPING = 600L;

  private final int serverPort = 8001;

  private static KafkaConsumer<MessageEnvelopePayment> KAFKA_CONSUMER;

  private static KafkaProducer<MessageEnvelopeStatus> KAFKA_PRODUCER;

  private String url;

  private String id;

  @BeforeAll
  public static void init() throws InterruptedException {
    Unirest.config().setObjectMapper(new GsonObjectMapper(Converters.registerAll(new GsonBuilder()).create()));

    Unirest.post(UrlUtil.getInstance().getSchemaRegistryPayment()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("schema", MessageEnvelopePayment.getClassSchema().toString())).asJson();
    Unirest.put(UrlUtil.getInstance().getSchemaRegistryCompatibilityPayment()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("compatibility", "Forward")).asJson();

    Unirest.post(UrlUtil.getInstance().getUrlSchemaRegistryStatus()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("schema", MessageEnvelopeStatus.getClassSchema().toString())).asJson();
    Unirest.put(UrlUtil.getInstance().getUrlSchemaRegistryCompatibilityStatus())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("compatibility", "Forward")).asJson();

    KAFKA_CONSUMER = new KafkaConsumer<>("payments.v1", (message, payloadClass) -> message.getPayload().getClass().equals(payloadClass));
    KAFKA_PRODUCER = new KafkaProducer<>("status.v1");
    TimeUnit.SECONDS.sleep(3);
  }

  @AfterAll
  public static void end() {
    KAFKA_PRODUCER.close();
    KAFKA_CONSUMER.close();
  }

  @BeforeEach
  public void before() {
    KAFKA_CONSUMER.before();
  }

  @Test
  @org.junit.jupiter.api.Order(1)
  public void shouldCreateOrderSaga() throws Exception {
    //Given
    final OrderRequestPayload orderRequestPayload =
        OrderRequestPayload.builder().productId(PRODUCT_ID).userId(USER_ID).price(PRICE).build();

    //When
    final HttpResponse<JsonNode> response = Unirest.post(String.format("http://localhost:%d/api/orders", this.serverPort))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).body(orderRequestPayload).asJson();
    //Then
    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(201));
    final List<String> locations = response.getHeaders().get(HttpHeaders.LOCATION);
    assertThat(locations, is(notNullValue()));
    assertThat(locations, is(hasSize(1)));
    this.url = locations.get(0);
    Awaitility.await().pollDelay(DELAY_MS, TimeUnit.MILLISECONDS).pollInterval(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
        .atMost(TIMEOUT_MS, TimeUnit.MILLISECONDS).until(() -> {
          final Tuple2<String, MessageEnvelopePayment> result = KAFKA_CONSUMER.expectMessagePipe(PaymentCreateCommand.class, TIMEOUT_MS);
          assertThat(result, is(notNullValue()));
          assertThat(result.getV1(), is(notNullValue()));
          assertThat(result.getV2(), is(notNullValue()));
          final PaymentCreateCommand paymentCreateCommand = PaymentCreateCommand.class.cast(result.getV2().getPayload());
          assertThat(paymentCreateCommand.getPurchaseId(), is(notNullValue()));
          this.id = paymentCreateCommand.getPurchaseId();
          assertThat(paymentCreateCommand.getPrice(), is(PRICE));
          assertThat(paymentCreateCommand.getUserId(), is(USER_ID));
          assertThat(paymentCreateCommand.getProductId(), is(PRODUCT_ID));
          assertThat(paymentCreateCommand.getSentAt(), is(notNullValue()));
          return true;
        });
  }

  @Test
  @org.junit.jupiter.api.Order(2)
  public void shouldUpdatePaymentOrderSaga() throws Exception {

    //Given
    final Payment payment = Payment.newBuilder()
        .setId(ID_PAYMENT)
        .setPurchaseId(this.id)
        .setUserId(USER_ID)
        .setProductId(PRODUCT_ID)
        .setStatus(PaymentStatus.ACCEPTED)
        .setPrice(PRICE)
        .setCreatedAt(Instant.now())
        .build();
    final PaymentCreatedEvent paymentCreatedEvent =
        PaymentCreatedEvent.newBuilder()
            .setPayment(payment)
            .setOperationStatus(PaymentOperationStatus.SUCCESS)
            .setUpdatedAt(Instant.now())
            .build();
    final MessageEnvelopeStatus messageEnvelopeStatus = MessageEnvelopeStatus.newBuilder()
        .setMetadata(this.createMetaData())
        .setPayload(paymentCreatedEvent)
        .build();
    //When
    KAFKA_PRODUCER.send(messageEnvelopeStatus, () -> this.id);

    //Then
    Awaitility.await().pollDelay(DELAY_MS, TimeUnit.MILLISECONDS).pollInterval(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
        .atMost(TIMEOUT_MS, TimeUnit.MILLISECONDS).until(() -> {
          this.assertOrderAggregateStatus(OrderAggregateStatusPayload.SHIPPING_SENT, this::validatePayment);
          return true;
        });
  }

  @Test
  @org.junit.jupiter.api.Order(3)
  public void shouldUpdateShippingOrderSaga() throws Exception {
    //Given
    final Shipping shipping = Shipping.newBuilder()
        .setId(ID_SHIPPING)
        .setPurchaseId(this.id)
        .setUserId(USER_ID)
        .setProductId(PRODUCT_ID)
        .setStatus(ShippingStatus.ACCEPTED)
        .setPrice(PRICE)
        .setCreatedAt(Instant.now())
        .build();
    final ShippingCreatedEvent shippingCreatedEvent =
        ShippingCreatedEvent.newBuilder()
            .setShipping(shipping)
            .setOperationStatus(ShippingOperationStatus.SUCCESS)
            .setUpdatedAt(Instant.now())
            .build();
    final MessageEnvelopeStatus messageEnvelopeStatus = MessageEnvelopeStatus.newBuilder()
        .setMetadata(this.createMetaData())
        .setPayload(shippingCreatedEvent)
        .build();
    //When
    KAFKA_PRODUCER.send(messageEnvelopeStatus, () -> this.id);

    //Then
    Awaitility.await().pollDelay(DELAY_MS, TimeUnit.MILLISECONDS).pollInterval(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
        .atMost(TIMEOUT_MS, TimeUnit.MILLISECONDS).until(() -> {
          this.assertOrderAggregateStatus(OrderAggregateStatusPayload.SUCCESS, this::validateShipping);
          return true;
        });
  }

  private void assertOrderAggregateStatus(final OrderAggregateStatusPayload status, final Predicate<OrderAggregatePayload> predicate) {
    final HttpResponse<OrderAggregatePayload> response = Unirest.get(this.url)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).asObject(OrderAggregatePayload.class);

    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(200));
    assertThat(response.getBody(), is(notNullValue()));
    final OrderAggregatePayload orderAggregatePayload = response.getBody();
    assertThat(orderAggregatePayload.getId(), is(this.id));
    assertThat(orderAggregatePayload.getStatus(), is(status));
    assertThat(predicate.test(orderAggregatePayload), is(true));
  }

  private boolean validateOrder(final OrderAggregatePayload orderAggregatePayload) {
    final OrderPayload order = orderAggregatePayload.getOrder();
    assertThat(order, is(notNullValue()));
    assertThat(order.getId(), is(notNullValue()));
    assertThat(order.getStatus(), is(OrderStatusPayload.CREATED));
    assertThat(order.getCreatedAt(), is(notNullValue()));
    return true;
  }

  private boolean validatePayment(final OrderAggregatePayload orderAggregatePayload) {
    final PaymentPayload payment = orderAggregatePayload.getPayment();
    assertThat(payment, is(notNullValue()));
    assertThat(payment.getId(), is(ID_PAYMENT));
    assertThat(payment.getStatus(), is(PaymentStatusPayload.ACCEPTED));
    assertThat(payment.getCreatedAt(), is(notNullValue()));
    return true;
  }

  private boolean validateShipping(final OrderAggregatePayload orderAggregatePayload) {
    final ShippingPayload shipping = orderAggregatePayload.getShipping();
    assertThat(shipping, is(notNullValue()));
    assertThat(shipping.getId(), is(ID_SHIPPING));
    assertThat(shipping.getStatus(), is(ShippingStatusPayload.ACCEPTED));
    assertThat(shipping.getCreatedAt(), is(notNullValue()));
    return this.validateOrder(orderAggregatePayload) && this.validatePayment(orderAggregatePayload);
  }

  protected Metadata createMetaData() {
    return Metadata.newBuilder().setDomain("order").setName("order").setAction("create").setVersion("vTest")
        .setTimestamp(DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())).build();
  }

}
