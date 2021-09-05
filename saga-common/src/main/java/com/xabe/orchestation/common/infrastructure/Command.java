package com.xabe.orchestation.common.infrastructure;

import com.xabe.orchestation.common.infrastructure.dispatch.CommandContext;
import io.smallrye.mutiny.Uni;

public interface Command<C extends CommandContext<T, U>, T extends AggregateRoot<U>, U> {

  U getAggregateRootId();

  Uni<T> execute(final C context);
}
