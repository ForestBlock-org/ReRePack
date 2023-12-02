package org.crayne.rerepack.syntax.parser.result;

import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.util.logging.message.AbstractLoggingMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ErrorResult implements ParseResult {

    @NotNull
    private final AbstractLoggingMessage errorMessage;

    public ErrorResult(@NotNull final AbstractLoggingMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int nextIndex() {
        return -1;
    }

    @NotNull
    public Optional<AbstractLoggingMessage> resultLogMessage() {
        return Optional.of(errorMessage);
    }

    @NotNull
    public Optional<Node> node() {
        return Optional.empty();
    }

    @NotNull
    public AbstractLoggingMessage errorMessage() {
        return errorMessage;
    }

    @NotNull
    public String toString() {
        return "ErrorResult{" +
                "errorMessage=" + errorMessage +
                '}';
    }

}
