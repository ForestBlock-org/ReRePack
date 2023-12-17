package org.crayne.rerepack.workspace.pack.write;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.Workspace;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.definition.Definition;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class CopyStatement extends WriteStatement {

    @NotNull
    private final Token sourcePath;

    @Nullable
    private Token initializedSourcePath;

    @NotNull
    private final Workspace workspace;

    public CopyStatement(@NotNull final Token destinationPath,
                         @NotNull final Token sourcePath,
                         @NotNull final DefinitionContainer definitionContainer,
                         @NotNull final Workspace workspace) {
        super(destinationPath, definitionContainer);
        this.sourcePath = sourcePath;
        this.workspace = workspace;
    }

    @NotNull
    public Optional<Token> initializedSourcePath() {
        return Optional.ofNullable(initializedSourcePath);
    }

    @NotNull
    public Token sourcePath() {
        return sourcePath;
    }

    public void initialize() throws WorkspaceException {
        initializedSourcePath = Definition.parseValueByDefinitions(sourcePath, definitionContainer());
        final File sourceFile = new File(workspace.directory(), initializedSourcePath.token());

        try {
            Files.readAllLines(sourceFile.toPath()).stream().map(s -> Token.of(s, sourcePath)).forEach(this.lines()::add);
        } catch (final IOException e) {
            throw new WorkspaceException("Could not copy source file '"
                    + sourceFile + "': " + e.getMessage(), sourcePath);
        }
        super.initialize();
    }

    public void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws WorkspaceException {

    }

}
