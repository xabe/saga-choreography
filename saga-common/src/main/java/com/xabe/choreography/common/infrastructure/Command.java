package com.xabe.choreography.common.infrastructure;

import com.xabe.choreography.common.infrastructure.dispatch.CommandContext;
import io.smallrye.mutiny.Uni;

public interface Command<C extends CommandContext<T, U>, T extends AggregateRoot<U>, U> {

  U getAggregateRootId();

  Uni<T> execute(final C context);
}
