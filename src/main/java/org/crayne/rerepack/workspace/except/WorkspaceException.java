package org.crayne.rerepack.workspace.except;

import org.crayne.rerepack.syntax.Token;
import org.jetbrains.annotations.NotNull;

public class WorkspaceException extends Exception {

    @NotNull
    private final Token[] traceBackTokens;

    public WorkspaceException(@NotNull final Token @NotNull ... traceBackTokens) {
        super();
        this.traceBackTokens = traceBackTokens;
    }

    public WorkspaceException(@NotNull final String s, @NotNull final Token @NotNull ... traceBackTokens) {
        super(s);
        this.traceBackTokens = traceBackTokens;
    }

    public WorkspaceException(@NotNull final Throwable t, @NotNull final Token @NotNull ... traceBackTokens) {
        super(t);
        this.traceBackTokens = traceBackTokens;
    }

    @NotNull
    public Token[] traceBackTokens() {
        return traceBackTokens;
    }

}
