package org.crayne.rerepack.workspace.pack.write;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.WRITE_STATEMENT;

public class WriteContainer implements Parseable, Initializable {

    @NotNull
    private final Set<WriteStatement> writeStatements;

    @NotNull
    private final DefinitionContainer definitionContainer;

    public WriteContainer(@NotNull final DefinitionContainer definitionContainer) {
        this.writeStatements = new HashSet<>();
        this.definitionContainer = definitionContainer;
    }

    @NotNull
    public WriteStatement createWriteStatement(@NotNull final Token destinationPath) {
        final WriteStatement writeStatement = new WriteStatement(destinationPath, definitionContainer);
        addWriteStatement(writeStatement);
        return writeStatement;
    }

    public void addWriteStatement(@NotNull final WriteStatement writeStatement) {
        writeStatements.add(writeStatement);
    }

    public void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws WorkspaceException {
        for (final Node writeStatementAST : ast.children(WRITE_STATEMENT)) {
            final Token destinationPath = writeStatementAST.child(1).valueClean();
            final WriteStatement writeStatement = createWriteStatement(destinationPath);
            writeStatement.parseFromAST(writeStatementAST, packScope);
        }
    }

    public void initialize() throws WorkspaceException {
        for (final WriteStatement writeStatement : writeStatements) writeStatement.initialize();
    }

    @NotNull
    public Set<WriteStatement> writeStatements() {
        return Collections.unmodifiableSet(writeStatements);
    }

    @NotNull
    public String toString() {
        return "WriteContainer{" +
                "writeStatements=" + writeStatements +
                ", definitionContainer=" + definitionContainer +
                '}';
    }

}
