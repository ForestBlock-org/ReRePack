package org.crayne.rerepack.syntax.parser.except;

import org.jetbrains.annotations.NotNull;

public class ParserException extends RuntimeException {

    public ParserException() {
        super();
    }

    public ParserException(@NotNull final String message) {
        super(message);
    }

    public ParserException(@NotNull final Throwable t) {
        super(t);
    }

}
