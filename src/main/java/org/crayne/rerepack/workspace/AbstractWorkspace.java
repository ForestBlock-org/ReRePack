package org.crayne.rerepack.workspace;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.workspace.util.def.DefinitionContainer;
import org.crayne.rerepack.workspace.util.replacement.predicate.Predicate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractWorkspace {

    @NotNull
    private final DefinitionContainer definitionContainer;

    @NotNull
    private final Map<String, List<String>> fileWritePredicates;

    public AbstractWorkspace() {
        this.definitionContainer = new DefinitionContainer();
        this.fileWritePredicates = new HashMap<>();
    }

    @NotNull
    public DefinitionContainer definitionContainer() {
        return definitionContainer;
    }

    @NotNull
    public Map<String, List<String>> fileWritePredicates() {
        return fileWritePredicates;
    }

    public void createFileWritePredicate(@NotNull final Token filepathToken, @NotNull final List<Token> lines) {
        fileWritePredicates.put(filepathToken.token(), lines.stream()
                .map(t -> Predicate.parseDefinitions(t, t, definitionContainer))
                .map(Token::token)
                .toList());
    }

    public void createDefinition(@NotNull final Token name, @NotNull final Token value) {
        definitionContainer.define(name, value);
    }

    public void initializeDefinitions() {
        definitionContainer.definitions().forEach((key, value) -> definitionContainer.initialize(key));
    }

}
