package org.crayne.rerepack.workspace.util.replacement.template;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.workspace.AbstractWorkspace;
import org.crayne.rerepack.workspace.util.def.DefinitionContainer;
import org.crayne.rerepack.workspace.util.except.DefinitionException;
import org.crayne.rerepack.workspace.util.pack.Pack;
import org.crayne.rerepack.workspace.util.replacement.predicate.Predicate;
import org.crayne.rerepack.workspace.util.replacement.Replacement;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class Template extends AbstractWorkspace {

    @NotNull
    private final Pack pack;

    @NotNull
    private final Map<Token, Optional<Token>> parameterNames;

    @NotNull
    private final Set<Predicate> matches, replacements;

    public Template(@NotNull final Pack pack, @NotNull final Map<Token, Optional<Token>> parameterNames,
                    @NotNull final Set<Predicate> matches, @NotNull final Set<Predicate> replacements) {
        super();
        this.pack = pack;
        this.parameterNames = new LinkedHashMap<>(parameterNames);
        this.matches = new HashSet<>(matches);
        this.replacements = new HashSet<>(replacements);
    }

    public void matchAll(@NotNull final Set<Predicate> matches,
                         @NotNull final Set<Predicate> replacements) {
        matches.forEach(this::match);
        replacements.forEach(this::replace);
    }

    public void match(@NotNull final Predicate match) {
        matches.add(match);
    }

    public void replace(@NotNull final Predicate replace) {
        replacements.add(replace);
    }

    private void finalizeReplacements(@NotNull final Set<Predicate> predicates,
                                             @NotNull final Consumer<Predicate> predicateConsumer,
                                             @NotNull final DefinitionContainer parameters) {
        predicates.forEach(p -> {
            final Token key =   Predicate.parseDefinitions(p.key(),   p.key(),   parameters, definitionContainer());
            final Token value = Predicate.parseDefinitions(p.value(), p.value(), parameters, definitionContainer());
            predicateConsumer.accept(new Predicate(key, value));
        });
    }

    private void handleMissingParameters(@NotNull final Token name, @NotNull final DefinitionContainer parameters) {
        for (@NotNull final Token requiredParameter : parameterNames.keySet()) {
            if (parameters.defined(requiredParameter)) continue;
            final Optional<Token> defaultValue = parameterNames.get(requiredParameter);

            if (defaultValue.isPresent()) {
                parameters.define(requiredParameter, defaultValue.get());
                continue;
            }
            throw new DefinitionException(name, "Cannot use template; Missing parameter '" + requiredParameter.token() + "'");
        }
    }

    @NotNull
    public Replacement createReplacement(@NotNull final Token name, @NotNull final DefinitionContainer parameters) {
        handleMissingParameters(name, parameters);
        final Replacement replacement = new Replacement(pack);
        finalizeReplacements(matches, replacement::match, parameters);
        finalizeReplacements(replacements, replacement::replace, parameters);

        return replacement;
    }

}
