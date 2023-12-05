package org.crayne.rerepack.workspace.except;

import org.crayne.rerepack.syntax.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class DefinitionException extends WorkspaceException {

    public DefinitionException(@NotNull final Token @NotNull ... traceBackTokens) {
        super(traceBackTokens);
    }

    public DefinitionException(@NotNull final String s, @NotNull final Token @NotNull ... traceBackTokens) {
        super(s, traceBackTokens);
    }

    public DefinitionException(@NotNull final Throwable t, @NotNull final Token @NotNull ... traceBackTokens) {
        super(t, traceBackTokens);
    }

    public DefinitionException(@NotNull final Collection<Token> traceBackTokens) {
        super(traceBackTokens);
    }

    public DefinitionException(@NotNull final String s, @NotNull final Collection<Token> traceBackTokens) {
        super(s, traceBackTokens);
    }

    public DefinitionException(@NotNull final Throwable t, @NotNull final Collection<Token> traceBackTokens) {
        super(t, traceBackTokens);
    }

}
