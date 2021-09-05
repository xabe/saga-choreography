package com.xabe.orchestation.common.infrastructure.event;

import com.xabe.orchestation.common.infrastructure.Event;

public interface EventPublisher {

  void tryPublish(Event event);

}
