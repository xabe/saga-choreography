package com.xabe.orchestation.common.infrastructure.event;

import com.xabe.orchestation.common.infrastructure.Event;

public interface EventConsumer {

  void consume(final Event event);

}
