package org.crayne.rerepack.workspace.pack.definition;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.workspace.except.DefinitionException;
import org.crayne.rerepack.workspace.predicate.TokenPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Definition {

    @NotNull
    private final TokenPredicate definition;

    @Nullable
    private Token initializedValue;

    public Definition(@NotNull final TokenPredicate definition) {
        this.definition = definition;
    }

    public Definition(@NotNull final Token identifier, @NotNull final Token value) {
        this.definition = new TokenPredicate(identifier, value);
    }

    @NotNull
    public TokenPredicate definition() {
        return definition;
    }

    @NotNull
    public Optional<Token> initializedValue() {
        return Optional.ofNullable(initializedValue);
    }

    @NotNull
    public String toString() {
        return definition.toString();
    }

    public void initializeDefinition(@NotNull final DefinitionContainer definitionContainer) throws DefinitionException {
        initializeDefinition(definitionContainer, new HashSet<>());
    }

    public void initializeDefinition(@NotNull final DefinitionContainer definitionContainer,
                                     @NotNull final Set<Token> currentlyDefining) throws DefinitionException {
        if (initializedValue != null) return;

        final Token definedAt = definition.key();
        final Token initializedAt = definition.value();

        currentlyDefining.add(definedAt);

        String result = initializedAt.toString();

        final Matcher definitionMatcher = Pattern.compile("\\$\\((.*?)\\)").matcher(result);

        while (definitionMatcher.find()) {
            final Token definitionName = Token.of(definitionMatcher.group(1), definedAt);
            final Definition definitionValue = definitionContainer.definition(definitionName);
            final boolean uninitialized = definitionValue.initializedValue().isEmpty();

            if (uninitialized) {
                final boolean cyclicDefinition = currentlyDefining.contains(definitionName);
                if (cyclicDefinition)
                    throw new DefinitionException("Invalid use of uninitialized variable '" + definitionName + "'",
                            initializedAt, definitionValue.definition.key());

                definitionValue.initializeDefinition(definitionContainer, currentlyDefining);
            }

            result = result.replace("$(" + definitionName + ")", definitionValue.toString());
        }
        initializedValue = Token.of(result, initializedAt);
    }

    public int hashCode() {
        return definition.hashCode();
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Definition that = (Definition) o;

        return definition.equals(that.definition);
    }


}
