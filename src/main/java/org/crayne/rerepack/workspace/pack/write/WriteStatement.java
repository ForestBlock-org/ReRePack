package org.crayne.rerepack.workspace.pack.write;

import org.crayne.rerepack.syntax.Token;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WriteStatement {

    @NotNull
    private final List<Token> lines;

    @NotNull
    private final Token destinationPath;

    public WriteStatement(@NotNull final Token destinationPath, @NotNull final List<Token> lines) {
        this.destinationPath = destinationPath;
        this.lines = new ArrayList<>(lines);
    }

    public WriteStatement(@NotNull final Token destinationPath) {
        this.destinationPath = destinationPath;
        this.lines = new ArrayList<>();
    }

    public void addLine(@NotNull final Token lineToken) {
        lines.add(lineToken);
    }

    @NotNull
    public Token destinationPath() {
        return destinationPath;
    }

    @NotNull
    public List<Token> lines() {
        return Collections.unmodifiableList(lines);
    }


}
