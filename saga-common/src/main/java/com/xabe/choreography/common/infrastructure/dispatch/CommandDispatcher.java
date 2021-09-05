package com.xabe.choreography.common.infrastructure.dispatch;

import com.xabe.choreography.common.infrastructure.AggregateRoot;
import com.xabe.choreography.common.infrastructure.Command;
import io.smallrye.mutiny.Uni;

public interface CommandDispatcher<C extends CommandContext<T, U>, T extends AggregateRoot<U>, U> {

  Uni<T> dispatch(final Command<C, T, U> command);

}
