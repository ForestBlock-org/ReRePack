package org.crayne.rerepack.workspace.except;

import org.crayne.rerepack.syntax.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class WorkspaceException extends Exception {

    @NotNull
    private final List<Token> traceBackTokens;

    public WorkspaceException(@NotNull final Token @NotNull ... traceBackTokens) {
        super();
        this.traceBackTokens = Arrays.stream(traceBackTokens).toList();
    }

    public WorkspaceException(@NotNull final String s, @NotNull final Token @NotNull ... traceBackTokens) {
        super(s);
        this.traceBackTokens = Arrays.stream(traceBackTokens).toList();
    }

    public WorkspaceException(@NotNull final Throwable t, @NotNull final Token @NotNull ... traceBackTokens) {
        super(t);
        this.traceBackTokens = Arrays.stream(traceBackTokens).toList();
    }

    public WorkspaceException(@NotNull final Collection<Token> traceBackTokens) {
        super();
        this.traceBackTokens = traceBackTokens.stream().toList();
    }

    public WorkspaceException(@NotNull final String s, @NotNull final Collection<Token> traceBackTokens) {
        super(s);
        this.traceBackTokens = traceBackTokens.stream().toList();
    }

    public WorkspaceException(@NotNull final Throwable t, @NotNull final Collection<Token> traceBackTokens) {
        super(t);
        this.traceBackTokens = traceBackTokens.stream().toList();
    }

    @NotNull
    public List<Token> traceBackTokens() {
        return traceBackTokens;
    }

}
