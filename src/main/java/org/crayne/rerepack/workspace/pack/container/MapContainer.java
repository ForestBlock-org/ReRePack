package org.crayne.rerepack.workspace.pack.container;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.workspace.except.DefinitionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class MapContainer<T> {

    @NotNull
    private final Map<Token, T> definitions;

    @Nullable
    private final MapContainer<T> parent;

    public MapContainer() {
        this.definitions = new LinkedHashMap<>();
        this.parent = null;
    }

    public MapContainer(@NotNull final MapContainer<T> parent) {
        this.definitions = new LinkedHashMap<>();
        this.parent = parent;
    }

    public MapContainer(@NotNull final MapContainer<T> parent, @NotNull final MapContainer<T> copyAll) throws DefinitionException {
        this.definitions = new LinkedHashMap<>();
        this.parent = parent;
        defineAll(copyAll);
    }

    @NotNull
    public Map<Token, T> definitions() {
        return Collections.unmodifiableMap(definitions);
    }

    @NotNull
    public T addDefinition(@NotNull final Token identifier, @NotNull final T definition) throws DefinitionException {
        handleRedefined(identifier);

        definitions.put(identifier, definition);
        return definition;
    }

    @NotNull
    public String cannotRedefinePreviousDefinitionString() {
        return "Cannot redefine previous definition";
    }

    @NotNull
    public String cannotFindDefinitionString() {
        return "Cannot find definition";
    }

    protected void handleRedefined(@NotNull final Token identifier) throws DefinitionException {
        final Optional<T> previousDefinition = findDefinition(identifier);
        if (previousDefinition.isEmpty()) return;

        final Optional<Token> previousDefinitionToken = findDefiningToken(previousDefinition.get());

        if (previousDefinitionToken.isEmpty())
            throw new DefinitionException(cannotRedefinePreviousDefinitionString() + " " + previousDefinition.get(), identifier);

        throw new DefinitionException(cannotRedefinePreviousDefinitionString() + " " + previousDefinition.get(),
                identifier, previousDefinitionToken.get());
    }

    @NotNull
    public Optional<MapContainer<T>> parent() {
        return Optional.ofNullable(parent);
    }

    @NotNull
    public Optional<Token> findDefiningToken(@NotNull final T value) {
        final Optional<Token> foundHere = definitions.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(value))
                .map(Map.Entry::getKey)
                .findAny();

        final Optional<Token> foundInParent = parent == null
                ? Optional.empty()
                : parent.findDefiningToken(value);

        final Token foundAny = foundHere.orElse(foundInParent.orElse(null));
        return Optional.ofNullable(foundAny);
    }

    @NotNull
    public Optional<T> findDefinition(@NotNull final Token identifier) {
        final Optional<T> definitionHere = Optional.ofNullable(definitions.get(identifier));
        final Optional<T> definitionParent = parent == null ? Optional.empty() : parent.findDefinition(identifier);
        final T anyDefinition = definitionHere.orElse(definitionParent.orElse(null));

        return Optional.ofNullable(anyDefinition);
    }

    @NotNull
    public T definition(@NotNull final Token identifier) throws DefinitionException {
        return findDefinition(identifier).orElseThrow(() ->
                new DefinitionException(cannotFindDefinitionString() + " '" + identifier + "'", identifier));
    }

    public void defineAll(@NotNull final MapContainer<T> other) throws DefinitionException {
        for (final Map.Entry<Token, T> entry : other.definitions.entrySet())
            addDefinition(entry.getKey(), entry.getValue());
    }

    @NotNull
    public String toString() {
        return "MapContainer{" +
                "definitions=" + definitions +
                //", parent=" + parent +
                '}';
    }

}
