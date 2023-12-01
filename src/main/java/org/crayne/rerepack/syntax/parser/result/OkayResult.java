package org.crayne.rerepack.syntax.parser.result;

import org.crayne.rerepack.util.logging.message.AbstractLoggingMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OkayResult implements ParseResult {

    private final int nextIndex;

    public OkayResult(final int nextIndex) {
        this.nextIndex = nextIndex;
    }

    public int nextIndex() {
        return nextIndex;
    }

    @NotNull
    public Optional<AbstractLoggingMessage> resultLogMessage() {
        return Optional.empty();
    }

}
