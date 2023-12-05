package org.crayne.rerepack.syntax.parser.except;

import org.jetbrains.annotations.NotNull;

public class SyntaxException extends RuntimeException {

    public SyntaxException() {
        super();
    }

    public SyntaxException(@NotNull final String message) {
        super(message);
    }

    public SyntaxException(@NotNull final Throwable t) {
        super(t);
    }

}
