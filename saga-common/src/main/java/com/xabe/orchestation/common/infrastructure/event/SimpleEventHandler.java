package com.xabe.orchestation.common.infrastructure.event;

import com.xabe.orchestation.common.infrastructure.Event;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.avro.specific.SpecificRecord;

public class SimpleEventHandler<T extends SpecificRecord, R extends Event> implements EventHandler<T> {

  private final Function<T, R> mapper;

  private final Consumer<R> consumer;

  public SimpleEventHandler(final Function<T, R> mapper, final Consumer<R> consumer) {
    this.mapper = mapper;
    this.consumer = consumer;
  }

  @Override
  public void handle(final T paylooad) {
    this.consumer.accept(this.mapper.apply(paylooad));
  }
}
