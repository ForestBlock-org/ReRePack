package org.crayne.rerepack.workspace.pack.definition;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.workspace.except.DefinitionException;
import org.crayne.rerepack.workspace.pack.container.MapContainer;
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
    public Token fullValue() {
        return initializedValue().orElse(definition.value());
    }

    @NotNull
    public TokenPredicate fullDefinition() {
        return new TokenPredicate(definition.key(), fullValue());
    }

    @NotNull
    public Optional<Token> initializedValue() {
        return Optional.ofNullable(initializedValue);
    }

    @NotNull
    public String toString() {
        return definition.key() + " = \"" + fullValue() + "\"";
    }

    public void initializeDefinition(@NotNull final MapContainer<Definition> definitionContainer) throws DefinitionException {
        initializeDefinition(definitionContainer, new HashSet<>());
    }

    public void initializeDefinition(@NotNull final MapContainer<Definition> definitionContainer,
                                     @NotNull final Set<Token> currentlyDefining) throws DefinitionException {
        if (initializedValue != null) return;

        final Token definedAt = definition.key();
        final Token initializedAt = definition.value();

        currentlyDefining.add(definedAt);
        initializedValue = parseValueByDefinitions(initializedAt, definitionContainer, currentlyDefining);
    }

    @NotNull
    public static Token parseValueByDefinitions(@NotNull final Token value,
                                                @NotNull final MapContainer<Definition> definitionContainer) throws DefinitionException {
        return parseValueByDefinitions(value, definitionContainer, false);
    }

    @NotNull
    public static Token parseValueByDefinitions(@NotNull final Token value,
                                                @NotNull final MapContainer<Definition> definitionContainer,
                                                final boolean ignoreInitialization) throws DefinitionException {
        return parseValueByDefinitions(value, definitionContainer, new HashSet<>(), ignoreInitialization);
    }

    @NotNull
    public static Token parseValueByDefinitions(@NotNull final Token value,
                                                @NotNull final MapContainer<Definition> definitionContainer,
                                                @NotNull final Set<Token> currentlyDefining) throws DefinitionException {
        return parseValueByDefinitions(value, definitionContainer, currentlyDefining, false);
    }

    public static void ensureValidDefinition(@NotNull final Token value,
                                             @NotNull final DefinitionContainer definitionContainer) throws DefinitionException {
        parseValueByDefinitions(value, definitionContainer, true);
    }

    public static void ensureValidDefinition(@NotNull final TokenPredicate values,
                                             @NotNull final DefinitionContainer definitionContainer) throws DefinitionException {
        ensureValidDefinition(values.key(), definitionContainer);
        ensureValidDefinition(values.value(), definitionContainer);
    }

    public static void initializeDefinition(@NotNull final TokenPredicate values,
                                             @NotNull final DefinitionContainer definitionContainer) throws DefinitionException {
        values.key(parseValueByDefinitions(values.key(), definitionContainer));
        values.value(parseValueByDefinitions(values.value(), definitionContainer));
    }

    @NotNull
    public static Token parseValueByDefinitions(@NotNull final Token value,
                                                @NotNull final MapContainer<Definition> definitionContainer,
                                                @NotNull final Set<Token> currentlyDefining,
                                                final boolean ignoreInitialization) throws DefinitionException {
        String result = value.toString();
        final Matcher definitionMatcher = Pattern.compile("\\$\\((.*?)\\)").matcher(result);

        while (definitionMatcher.find()) {
            final Token definitionName = Token.of(definitionMatcher.group(1), value);
            final Definition definitionValue = definitionContainer.definition(definitionName);
            if (ignoreInitialization) continue;

            final boolean uninitialized = definitionValue.initializedValue().isEmpty();

            if (uninitialized) {
                final boolean cyclicDefinition = currentlyDefining.contains(definitionName);
                if (cyclicDefinition)
                    throw new DefinitionException("Invalid use of uninitialized variable '" + definitionName + "'",
                            value, definitionValue.definition.key());

                definitionValue.initializeDefinition(definitionContainer, currentlyDefining);
            }
            result = result.replace("$(" + definitionName + ")",
                    definitionValue.initializedValue().orElseThrow().toString());
        }
        return Token.of(result, value);
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
