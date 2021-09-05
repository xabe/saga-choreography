package com.xabe.orchestation.common.infrastructure.dispatch;

import com.xabe.orchestation.common.infrastructure.AggregateRoot;
import com.xabe.orchestation.common.infrastructure.Command;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandDispatcherImpl<C extends CommandContext<T, U>, T extends AggregateRoot<U>, U>
    implements CommandDispatcher<C, T, U> {

  private final Logger LOG = LoggerFactory.getLogger(CommandDispatcherImpl.class);

  private final C context;

  public CommandDispatcherImpl(final C context) {
    this.context = context;
  }

  public C getContext() {
    return this.context;
  }

  @Override
  public Uni<T> dispatch(final Command<C, T, U> command) {
    return command.execute(this.context);
  }
}
