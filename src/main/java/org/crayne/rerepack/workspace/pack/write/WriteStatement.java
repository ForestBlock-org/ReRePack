package org.crayne.rerepack.workspace.pack.write;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.definition.Definition;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WriteStatement implements Parseable, Initializable {

    @NotNull
    private final List<Token> lines;

    @NotNull
    private final Token destinationPath;

    @NotNull
    private final DefinitionContainer definitionContainer;

    public WriteStatement(@NotNull final Token destinationPath, @NotNull final DefinitionContainer definitionContainer) {
        this.destinationPath = destinationPath;
        this.lines = new ArrayList<>();
        this.definitionContainer = definitionContainer;
    }

    public WriteStatement(@NotNull final DefinitionContainer definitionContainer,
                          @NotNull final Token destinationPath,
                          @NotNull final List<Token> lines) {
        this.destinationPath = destinationPath;
        this.lines = new ArrayList<>(lines);
        this.definitionContainer = definitionContainer;
    }

    public void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws WorkspaceException {
        final Node writeScope = ast.child(3);
        final List<Token> lines = writeScope.children()
                .stream()
                .map(n -> n.child(0))
                .map(Node::valueClean)
                .toList();

        for (final Token line : lines) {
            Definition.parseValueByDefinitions(line, definitionContainer, true);
            // only for error handling, no need to actually store the values for now (as this could be in a
            // template statement in which case the value is always uninitialized

            addLine(line);
        }
    }

    public void initialize() throws WorkspaceException {
        final List<Token> resultLines = new ArrayList<>();

        for (final Token line : lines)
            resultLines.add(Definition.parseValueByDefinitions(line, definitionContainer));

        lines.clear();
        lines.addAll(resultLines);
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

    @NotNull
    public String toString() {
        return "WriteStatement{" +
                "lines=" + lines +
                ", destinationPath=" + destinationPath +
                ", definitionContainer=" + definitionContainer +
                '}';
    }

}
