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
import java.util.stream.Collectors;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.*;

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
    public CopyStatement createCopyStatement(@NotNull final Token destinationPath,
                                             @NotNull final Token sourcePath, final boolean raw) {
        final CopyStatement writeStatement = new CopyStatement(destinationPath, sourcePath,
                definitionContainer, workspace, raw);
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
            final CopyStatement copyStatement = createCopyStatement(destinationPath, sourcePath, false);
            copyStatement.parseFromAST(copyStatementAST, packScope);
        }
        for (final Node copyStatementAST : ast.children(COPY_STATEMENT_RAW)) {
            final Token sourcePath = copyStatementAST.child(2).valueClean();
            final Token destinationPath = copyStatementAST.child(4).valueClean();
            final CopyStatement copyStatement = createCopyStatement(destinationPath, sourcePath, true);
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
    public Set<CopyStatement> copyStatementsRaw() {
        return copyStatements.stream().filter(CopyStatement::raw).collect(Collectors.toUnmodifiableSet());
    }

    @NotNull
    public Set<CopyStatement> copyStatementsFull() {
        return copyStatements.stream().filter(c -> !c.raw()).collect(Collectors.toUnmodifiableSet());
    }

    @NotNull
    public Set<CopyStatement> allCopyStatements() {
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
