package com.xabe.choreography.common.infrastructure.event;

import com.xabe.choreography.common.infrastructure.Event;

public interface EventPublisher {

  void tryPublish(Event event);

}
