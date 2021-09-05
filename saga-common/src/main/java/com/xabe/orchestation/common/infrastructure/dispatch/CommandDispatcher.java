package com.xabe.orchestation.common.infrastructure.dispatch;

import com.xabe.orchestation.common.infrastructure.AggregateRoot;
import com.xabe.orchestation.common.infrastructure.Command;
import io.smallrye.mutiny.Uni;

public interface CommandDispatcher<C extends CommandContext<T, U>, T extends AggregateRoot<U>, U> {

  Uni<T> dispatch(final Command<C, T, U> command);

}
