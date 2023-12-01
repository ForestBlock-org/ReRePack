package org.crayne.rerepack.workspace.util.def;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.workspace.util.except.DefinitionException;
import org.crayne.rerepack.workspace.util.replacement.predicate.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DefinitionContainer {

    @NotNull
    private final Map<Token, Token> definitions;

    @NotNull
    private final Set<Token> initialized;

    @Nullable
    private final DefinitionContainer parent;

    public DefinitionContainer(@NotNull final DefinitionContainer parent) {
        this.definitions = new LinkedHashMap<>();
        this.initialized = new HashSet<>();
        this.parent = parent;
    }

    public DefinitionContainer() {
        this.definitions = new LinkedHashMap<>();
        this.initialized = new HashSet<>();
        this.parent = null;
    }

    @NotNull
    public Map<Token, Token> definitions() {
        return Collections.unmodifiableMap(definitions);
    }

    @NotNull
    public Optional<DefinitionContainer> parent() {
        return Optional.ofNullable(parent);
    }

    public boolean defined(@NotNull final Token name) {
        return definitions.containsKey(name) || (parent != null && parent.defined(name));
    }

    public void define(@NotNull final Token name, @NotNull final Token value) {
        if (defined(name)) throw new DefinitionException(name, "Definition '" + name.token() + "' already exists; Cannot override");

        definitions.put(name, value);
    }

    public void initialize(@NotNull final Token name) {
        if (!defined(name)) throw new DefinitionException(name, "Definition '" + name.token() + "' was not found; Cannot initialize");

        final Token value = definitions.get(name);
        definitions.put(name, Predicate.parseDefinitions(name, value, this));
        initialized.add(name);
    }

    @NotNull
    public Optional<Token> findDefinition(@NotNull final Token name) {
        final Optional<Token> localDefinition = Optional.ofNullable(definitions.get(name));
        final Optional<Token> parentDefinition = parent == null ? Optional.empty() : parent.findDefinition(name);

        return Optional.ofNullable(localDefinition.orElse(parentDefinition.orElse(null)));
    }

    @NotNull
    public Token definition(@NotNull final Token name, @NotNull final Token value) {
        return findDefinition(name).orElseThrow(() -> new DefinitionException(value, "Cannot find definition '" + name.token() + "'"));
    }

    public boolean definitionInitialized(@NotNull final Token name) {
        return initialized.contains(name) || (parent != null && parent.definitionInitialized(name));
    }

}
