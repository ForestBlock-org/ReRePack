package org.crayne.rerepack.workspace.parse.parseable;

import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.jetbrains.annotations.NotNull;

public interface Parseable {

    void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws WorkspaceException;

}
