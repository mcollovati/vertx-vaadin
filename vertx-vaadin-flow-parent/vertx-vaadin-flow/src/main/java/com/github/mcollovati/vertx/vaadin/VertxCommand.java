package com.github.mcollovati.vertx.vaadin;

import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.ErrorHandlingCommand;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

/**
 * Vaadin {@link Command} implementation that executes logic
 * on vertx contex.
 *
 * @param <T> Commant type
 */
class VertxCommand<T extends Command> implements Command {
    final Context context;
    final T delegate;

    public VertxCommand(Context context, T delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    @Override
    public void execute() {
        runOnContext(delegate::execute);
    }

    protected final void runOnContext(Runnable runnable) {
        if (context == Vertx.currentContext()) {
            runnable.run();
        } else {
            context.executeBlocking(p -> {
                try {
                    runnable.run();
                } finally {
                    p.complete();
                }
            }, true, null);
        }
    }

    static Command wrap(Context context, Command command) {
        if (command instanceof ErrorHandlingCommand) {
            return new VertxErrorHandlingCommand(context, (ErrorHandlingCommand) command);
        } else {
            return new VertxCommand<>(context, command);
        }
    }

    private static class VertxErrorHandlingCommand extends VertxCommand<ErrorHandlingCommand> implements ErrorHandlingCommand {

        public VertxErrorHandlingCommand(Context context, ErrorHandlingCommand delegate) {
            super(context, delegate);
        }

        @Override
        public void handleError(Exception exception) {
            runOnContext(() -> delegate.handleError(exception));
        }
    }

}
