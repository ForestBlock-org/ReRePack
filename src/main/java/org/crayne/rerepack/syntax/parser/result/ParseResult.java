package org.crayne.rerepack.syntax.parser.result;

import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.util.logging.message.AbstractLoggingMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ParseResult {

    int nextIndex();

    @NotNull
    Optional<AbstractLoggingMessage> resultLogMessage();

    @NotNull
    Optional<Node> node();

    @NotNull
    static ErrorResult error(@NotNull final AbstractLoggingMessage errorMessage) {
        return new ErrorResult(errorMessage);
    }

    @NotNull
    static OkayResult ok(final int nextIndex, @NotNull final Node node) {
        return new OkayResult(nextIndex, node);
    }

    default boolean ok() {
        return this instanceof OkayResult;
    }

    default boolean error() {
        return this instanceof ErrorResult;
    }

}
