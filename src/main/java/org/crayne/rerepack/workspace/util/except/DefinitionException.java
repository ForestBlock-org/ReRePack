package org.crayne.rerepack.workspace.util.except;

import org.crayne.rerepack.syntax.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DefinitionException extends RuntimeException {

    @Nullable
    private final Token traceback;

    public DefinitionException() {
        super();
        this.traceback = null;
    }

    public DefinitionException(@NotNull final DefinitionException e) {
        super(e);
        this.traceback = e.traceback;
    }

    public DefinitionException(@NotNull final String s) {
        super(s);
        this.traceback = null;
    }

    public DefinitionException(@NotNull final Throwable t) {
        super(t);
        this.traceback = null;
    }

    public DefinitionException(@NotNull final Token at) {
        super();
        this.traceback = at;
    }

    public DefinitionException(@NotNull final Token at, @NotNull final String s) {
        super(s);
        this.traceback = at;
    }

    public DefinitionException(@NotNull final Token at, @NotNull final Throwable t) {
        super(t);
        this.traceback = at;
    }

    @NotNull
    public Optional<Token> traceback() {
        return Optional.ofNullable(traceback);
    }
}
