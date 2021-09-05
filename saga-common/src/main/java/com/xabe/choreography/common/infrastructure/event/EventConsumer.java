package com.xabe.choreography.common.infrastructure.event;

import com.xabe.choreography.common.infrastructure.Event;

public interface EventConsumer {

  void consume(final Event event);

}
