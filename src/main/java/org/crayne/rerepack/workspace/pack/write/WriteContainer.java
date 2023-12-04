package org.crayne.rerepack.workspace.pack.write;

import org.crayne.rerepack.syntax.Token;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WriteContainer {

    @NotNull
    private final Set<WriteStatement> writeStatements;

    public WriteContainer() {
        this.writeStatements = new HashSet<>();
    }

    @NotNull
    public WriteStatement createWriteStatement(@NotNull final Token destinationPath,
                                               @NotNull final List<Token> lines) {
        final WriteStatement writeStatement = new WriteStatement(destinationPath, lines);
        writeStatements.add(writeStatement);
        return writeStatement;
    }

    @NotNull
    public WriteStatement createWriteStatement(@NotNull final Token destinationPath) {
        final WriteStatement writeStatement = new WriteStatement(destinationPath);
        writeStatements.add(writeStatement);
        return writeStatement;
    }

}
