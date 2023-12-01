package org.crayne.rerepack.workspace.util.pack;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.workspace.util.except.DefinitionException;
import org.jetbrains.annotations.NotNull;

public class PackException extends DefinitionException {

    public PackException() {

    }

    public PackException(@NotNull final DefinitionException e) {
        super(e);
    }

    public PackException(@NotNull final String s) {
        super(s);
    }

    public PackException(@NotNull final Throwable t) {
        super(t);
    }

    public PackException(@NotNull final Token at) {
        super(at);
    }

    public PackException(@NotNull final Token at, @NotNull final String s) {
        super(at, s);
    }

    public PackException(@NotNull final Token at, @NotNull final Throwable t) {
        super(at, t);
    }
}
