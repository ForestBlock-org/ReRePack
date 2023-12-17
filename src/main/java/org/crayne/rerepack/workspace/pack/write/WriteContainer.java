package org.crayne.rerepack.workspace.pack.write;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.Workspace;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.COPY_STATEMENT;
import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.WRITE_STATEMENT;

public class WriteContainer implements Parseable, Initializable {

    @NotNull
    private final Set<WriteStatement> writeStatements;

    @NotNull
    private final Set<CopyStatement> copyStatements;

    @NotNull
    private final DefinitionContainer definitionContainer;

    @NotNull
    private final Workspace workspace;

    public WriteContainer(@NotNull final DefinitionContainer definitionContainer,
                          @NotNull final Workspace workspace) {
        this.writeStatements = new HashSet<>();
        this.copyStatements = new HashSet<>();
        this.definitionContainer = definitionContainer;
        this.workspace = workspace;
    }

    @NotNull
    public WriteStatement createWriteStatement(@NotNull final Token destinationPath) {
        final WriteStatement writeStatement = new WriteStatement(destinationPath, definitionContainer);
        addWriteStatement(writeStatement);
        return writeStatement;
    }

    @NotNull
    public CopyStatement createCopyStatement(@NotNull final Token destinationPath, @NotNull final Token sourcePath) {
        final CopyStatement writeStatement = new CopyStatement(destinationPath, sourcePath,
                definitionContainer, workspace);
        addCopyStatement(writeStatement);
        return writeStatement;
    }

    public void addWriteStatement(@NotNull final WriteStatement writeStatement) {
        writeStatements.add(writeStatement);
    }

    public void addCopyStatement(@NotNull final CopyStatement copyStatement) {
        copyStatements.add(copyStatement);
    }

    public void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws WorkspaceException {
        for (final Node writeStatementAST : ast.children(WRITE_STATEMENT)) {
            final Token destinationPath = writeStatementAST.child(1).valueClean();
            final WriteStatement writeStatement = createWriteStatement(destinationPath);
            writeStatement.parseFromAST(writeStatementAST, packScope);
        }

        for (final Node copyStatementAST : ast.children(COPY_STATEMENT)) {
            final Token sourcePath = copyStatementAST.child(1).valueClean();
            final Token destinationPath = copyStatementAST.child(3).valueClean();
            final CopyStatement copyStatement = createCopyStatement(destinationPath, sourcePath);
            copyStatement.parseFromAST(copyStatementAST, packScope);
        }
    }

    public void initialize() throws WorkspaceException {
        for (final WriteStatement writeStatement : writeStatements) {
            if (writeStatement instanceof CopyStatement) continue;
            writeStatement.initialize();
        }
        for (final CopyStatement copyStatement : copyStatements) {
            copyStatement.initialize();
        }
    }

    @NotNull
    public Set<WriteStatement> writeStatements() {
        return Collections.unmodifiableSet(writeStatements);
    }

    @NotNull
    public Set<CopyStatement> copyStatements() {
        return Collections.unmodifiableSet(copyStatements);
    }

    @NotNull
    public Workspace workspace() {
        return workspace;
    }

    @NotNull
    public String toString() {
        return "WriteContainer{" +
                "writeStatements=" + writeStatements +
                ", copyStatements=" + copyStatements +
                ", definitionContainer=" + definitionContainer +
                '}';
    }
}
