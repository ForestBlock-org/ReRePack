package org.crayne.rerepack.syntax.parser.result;

import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.util.logging.message.AbstractLoggingMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OkayResult implements ParseResult {

    private final int nextIndex;

    @NotNull
    private final Node node;

    public OkayResult(final int nextIndex, @NotNull final Node node) {
        this.nextIndex = nextIndex;
        this.node = node;
    }

    public int nextIndex() {
        return nextIndex;
    }

    @NotNull
    public Optional<AbstractLoggingMessage> resultLogMessage() {
        return Optional.empty();
    }

    @NotNull
    public Optional<Node> node() {
        return Optional.of(node);
    }

    @NotNull
    public String toString() {
        return "OkayResult{" +
                "nextIndex=" + nextIndex +
                '}';
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final OkayResult that = (OkayResult) o;

        return nextIndex == that.nextIndex;
    }

    public int hashCode() {
        return nextIndex;
    }
}
