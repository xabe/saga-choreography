package com.xabe.choreography.shipping.infrastructure.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.xabe.avro.v1.MessageEnvelopeShipping;
import com.xabe.avro.v1.MessageEnvelopeStatus;
import com.xabe.avro.v1.Metadata;
import com.xabe.avro.v1.Shipping;
import com.xabe.avro.v1.ShippingCreateCommand;
import com.xabe.avro.v1.ShippingCreatedEvent;
import com.xabe.avro.v1.ShippingOperationStatus;
import com.xabe.choreography.integration.KafkaConsumer;
import com.xabe.choreography.integration.KafkaProducer;
import com.xabe.choreography.integration.UrlUtil;
import groovy.lang.Tuple2;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import kong.unirest.Unirest;
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

  public static final int DELAY_MS = 1500;

  public static final int POLL_INTERVAL_MS = 500;

  private static KafkaConsumer<MessageEnvelopeStatus> KAFKA_CONSUMER;

  private static KafkaProducer<MessageEnvelopeShipping> KAFKA_PRODUCER;

  private Long shippingId;

  @BeforeAll
  public static void init() throws InterruptedException {
    Unirest.post(UrlUtil.getInstance().getUrlSchemaRegistryShipping()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("schema", MessageEnvelopeShipping.getClassSchema().toString())).asJson();
    Unirest.put(UrlUtil.getInstance().getUrlSchemaRegistryCompatibilityShipping())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("compatibility", "Forward")).asJson();

    Unirest.post(UrlUtil.getInstance().getUrlSchemaRegistryStatus()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("schema", MessageEnvelopeStatus.getClassSchema().toString())).asJson();
    Unirest.put(UrlUtil.getInstance().getUrlSchemaRegistryCompatibilityStatus())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("compatibility", "Forward")).asJson();

    KAFKA_CONSUMER = new KafkaConsumer<>("status.v1", (message, payloadClass) -> message.getPayload().getClass().equals(payloadClass));
    KAFKA_PRODUCER = new KafkaProducer<>("shipments.v1");
    TimeUnit.SECONDS.sleep(5);
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
    final String purchaseId = "1111";
    final ShippingCreateCommand shippingCreateCommand =
        ShippingCreateCommand.newBuilder().setPurchaseId(purchaseId).setProductId("1").setUserId("2").setPrice(100L)
            .setSentAt(Instant.now())
            .build();
    final MessageEnvelopeShipping messageEnvelopeShipping = MessageEnvelopeShipping.newBuilder()
        .setMetadata(this.createMetaData())
        .setPayload(shippingCreateCommand)
        .build();

    //When
    KAFKA_PRODUCER.send(messageEnvelopeShipping, () -> purchaseId);

    //Then
    Awaitility.await().pollDelay(DELAY_MS, TimeUnit.MILLISECONDS).pollInterval(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
        .atMost(TIMEOUT_MS, TimeUnit.MILLISECONDS).until(() -> {
          final Tuple2<String, MessageEnvelopeStatus> result = KAFKA_CONSUMER.expectMessagePipe(ShippingCreatedEvent.class, TIMEOUT_MS);
          assertThat(result, is(notNullValue()));
          assertThat(result.getV1(), is(notNullValue()));
          assertThat(result.getV2(), is(notNullValue()));
          final ShippingCreatedEvent shippingCreatedEvent = ShippingCreatedEvent.class.cast(result.getV2().getPayload());
          assertThat(shippingCreatedEvent.getUpdatedAt(), is(notNullValue()));
          assertThat(shippingCreatedEvent.getOperationStatus(), is(ShippingOperationStatus.SUCCESS));
          final Shipping shipping = shippingCreatedEvent.getShipping();
          assertThat(shipping.getId(), is(notNullValue()));
          this.shippingId = shipping.getId();
          assertThat(shipping.getPurchaseId(), is(shippingCreateCommand.getPurchaseId()));
          assertThat(shipping.getProductId(), is(shippingCreateCommand.getProductId()));
          assertThat(shipping.getUserId(), is(shippingCreateCommand.getUserId()));
          assertThat(shipping.getPrice(), is(shippingCreateCommand.getPrice()));
          assertThat(shipping.getStatus().name(), is("ACCEPTED"));
          assertThat(shipping.getCreatedAt(), is(notNullValue()));
          return true;
        });
  }

  protected Metadata createMetaData() {
    return Metadata.newBuilder().setDomain("shipping").setName("shipping").setAction("create").setVersion("vTest")
        .setTimestamp(DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())).build();
  }

}
