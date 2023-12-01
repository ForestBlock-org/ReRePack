package org.crayne.rerepack.workspace;

import org.jetbrains.annotations.NotNull;

public class WorkspaceException extends RuntimeException {

    public WorkspaceException() {
        super();
    }

    public WorkspaceException(@NotNull final String s) {
        super(s);
    }

    public WorkspaceException(@NotNull final Throwable t) {
        super(t);
    }

}
