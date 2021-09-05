package com.xabe.choreography.payment.infrastructure.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.xabe.avro.v1.MessageEnvelopePayment;
import com.xabe.avro.v1.MessageEnvelopeShipping;
import com.xabe.avro.v1.MessageEnvelopeStatus;
import com.xabe.avro.v1.Metadata;
import com.xabe.avro.v1.Payment;
import com.xabe.avro.v1.PaymentCreateCommand;
import com.xabe.avro.v1.PaymentCreatedEvent;
import com.xabe.avro.v1.PaymentOperationStatus;
import com.xabe.avro.v1.ShippingCreateCommand;
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

  private static KafkaConsumer<MessageEnvelopeStatus> KAFKA_CONSUMER_STATUS;

  private static KafkaConsumer<MessageEnvelopeShipping> KAFKA_CONSUMER_SHIPPING;

  private static KafkaProducer<MessageEnvelopePayment> KAFKA_PRODUCER;

  private Long paymentId;

  @BeforeAll
  public static void init() throws InterruptedException {
    Unirest.post(UrlUtil.getInstance().getSchemaRegistryPayment()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("schema", MessageEnvelopePayment.getClassSchema().toString())).asJson();
    Unirest.put(UrlUtil.getInstance().getSchemaRegistryCompatibilityPayment()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("compatibility", "Forward")).asJson();

    Unirest.post(UrlUtil.getInstance().getUrlSchemaRegistryStatus()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("schema", MessageEnvelopeStatus.getClassSchema().toString())).asJson();
    Unirest.put(UrlUtil.getInstance().getUrlSchemaRegistryCompatibilityStatus())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("compatibility", "Forward")).asJson();

    Unirest.post(UrlUtil.getInstance().getUrlSchemaRegistryShipping()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("schema", MessageEnvelopeShipping.getClassSchema().toString())).asJson();
    Unirest.put(UrlUtil.getInstance().getUrlSchemaRegistryCompatibilityShipping())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .body(Map.of("compatibility", "Forward")).asJson();

    KAFKA_CONSUMER_STATUS =
        new KafkaConsumer<>("status.v1", (message, payloadClass) -> message.getPayload().getClass().equals(payloadClass));
    KAFKA_CONSUMER_SHIPPING =
        new KafkaConsumer<>("shipments.v1", (message, payloadClass) -> message.getPayload().getClass().equals(payloadClass));
    KAFKA_PRODUCER = new KafkaProducer<>("payments.v1");
    TimeUnit.SECONDS.sleep(5);
  }

  @AfterAll
  public static void end() {
    KAFKA_PRODUCER.close();
    KAFKA_CONSUMER_STATUS.close();
  }

  @BeforeEach
  public void before() {
    KAFKA_CONSUMER_STATUS.before();
  }

  @Test
  @org.junit.jupiter.api.Order(1)
  public void shouldCreateOrderSaga() throws Exception {
    //Given
    final String purchaseId = "1111";
    final PaymentCreateCommand paymentCreateCommand =
        PaymentCreateCommand.newBuilder().setPurchaseId(purchaseId).setProductId("1").setUserId("2").setPrice(100L).setSentAt(Instant.now())
            .build();
    final MessageEnvelopePayment messageEnvelopePayment = MessageEnvelopePayment.newBuilder()
        .setMetadata(this.createMetaData())
        .setPayload(paymentCreateCommand)
        .build();

    //When
    KAFKA_PRODUCER.send(messageEnvelopePayment, () -> purchaseId);

    //Then
    Awaitility.await().pollDelay(DELAY_MS, TimeUnit.MILLISECONDS).pollInterval(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
        .atMost(TIMEOUT_MS, TimeUnit.MILLISECONDS).until(() -> {
          final Tuple2<String, MessageEnvelopeStatus> result = KAFKA_CONSUMER_STATUS.expectMessagePipe(PaymentCreatedEvent.class,
              TIMEOUT_MS);
          assertThat(result, is(notNullValue()));
          assertThat(result.getV1(), is(notNullValue()));
          assertThat(result.getV2(), is(notNullValue()));
          final PaymentCreatedEvent paymentCreatedEvent = PaymentCreatedEvent.class.cast(result.getV2().getPayload());
          assertThat(paymentCreatedEvent.getUpdatedAt(), is(notNullValue()));
          assertThat(paymentCreatedEvent.getOperationStatus(), is(PaymentOperationStatus.SUCCESS));
          final Payment payment = paymentCreatedEvent.getPayment();
          assertThat(payment.getId(), is(notNullValue()));
          this.paymentId = payment.getId();
          assertThat(payment.getPurchaseId(), is(paymentCreateCommand.getPurchaseId()));
          assertThat(payment.getProductId(), is(paymentCreateCommand.getProductId()));
          assertThat(payment.getUserId(), is(paymentCreateCommand.getUserId()));
          assertThat(payment.getPrice(), is(paymentCreateCommand.getPrice()));
          assertThat(payment.getStatus().name(), is("ACCEPTED"));
          assertThat(payment.getCreatedAt(), is(notNullValue()));
          final Tuple2<String, MessageEnvelopeShipping> resultShipping =
              KAFKA_CONSUMER_SHIPPING.expectMessagePipe(ShippingCreateCommand.class,
              TIMEOUT_MS);
          assertThat(resultShipping, is(notNullValue()));
          assertThat(resultShipping.getV1(), is(notNullValue()));
          assertThat(resultShipping.getV2(), is(notNullValue()));
          final ShippingCreateCommand shippingCreateCommand = ShippingCreateCommand.class.cast(resultShipping.getV2().getPayload());
          assertThat(shippingCreateCommand.getPurchaseId(), is(paymentCreateCommand.getPurchaseId()));
          assertThat(shippingCreateCommand.getPrice(), is(paymentCreateCommand.getPrice()));
          assertThat(shippingCreateCommand.getUserId(), is(paymentCreateCommand.getUserId()));
          assertThat(shippingCreateCommand.getProductId(), is(paymentCreateCommand.getProductId()));
          assertThat(shippingCreateCommand.getSentAt(), is(notNullValue()));
          return true;
        });
  }

  protected Metadata createMetaData() {
    return Metadata.newBuilder().setDomain("payment").setName("payment").setAction("create").setVersion("vTest")
        .setTimestamp(DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())).build();
  }

}
