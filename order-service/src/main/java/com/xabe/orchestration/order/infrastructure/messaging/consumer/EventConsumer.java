package com.xabe.orchestration.order.infrastructure.messaging.consumer;

import com.xabe.avro.v1.MessageEnvelopeStatus;
import com.xabe.orchestation.common.infrastructure.event.EventHandler;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import javax.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.slf4j.Logger;

@ApplicationScoped
@RequiredArgsConstructor
public class EventConsumer {

  private final Logger logger;

  private final Map<Class, EventHandler> handlers;

  @Incoming("status")
  public CompletionStage<Void> consumeKafka(final IncomingKafkaRecord<String, MessageEnvelopeStatus> message)
      throws ExecutionException, InterruptedException {
    final Metadata metadata = message.getMetadata();
    this.logger.info("Received a message. message: {} metadata {}", message, metadata);
    final MessageEnvelopeStatus messageEnvelopeOrder = message.getPayload();
    final Class<?> msgClass = messageEnvelopeOrder.getPayload().getClass();
    final SpecificRecord payload = SpecificRecord.class.cast(messageEnvelopeOrder.getPayload());
    final EventHandler handler = this.handlers.get(msgClass);
    if (Objects.isNull(handler)) {
      this.logger.warn("Received a non supported message. Type: {}, toString: {}", msgClass.getName(), payload);
    } else {
      this.logger.debug("Received a message. message: {} metadata {}", payload, metadata);
      handler.handle(payload);
    }
    return message.ack();
  }

}
