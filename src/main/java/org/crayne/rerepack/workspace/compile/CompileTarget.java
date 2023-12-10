package org.crayne.rerepack.workspace.compile;

import org.crayne.rerepack.workspace.Workspace;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface CompileTarget {

    @NotNull
    File outputDirectory();

    void compile(@NotNull final Workspace workspace);

}
