package org.crayne.rerepack.workspace.util.replacement;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.workspace.util.pack.Pack;
import org.crayne.rerepack.workspace.util.replacement.predicate.Predicate;
import org.crayne.rerepack.workspace.util.replacement.predicate.PredicateException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Replacement {

    @NotNull
    private final Map<Token, Token> matches, replacements;

    @NotNull
    private final Pack pack;

    public Replacement(@NotNull final Pack pack) {
        this.pack = pack;
        this.matches = new HashMap<>();
        this.replacements = new LinkedHashMap<>();
    }

    public Replacement(@NotNull final Pack pack, @NotNull final Set<Predicate> matches,
                       @NotNull final Set<Predicate> replacements) {
        this.pack = pack;
        this.matches = new HashMap<>();
        this.replacements = new LinkedHashMap<>();

        matchAll(matches, replacements);
    }

    @NotNull
    public Token parseValue(@NotNull final Token key, @NotNull final Token value) {
        return Predicate.parseDefinitions(key, value, pack.localDefinitionContainer());
    }

    public void matchAll(@NotNull final Set<Predicate> matches,
                         @NotNull final Set<Predicate> replacements) {
        matches.forEach(this::match);
        replacements.forEach(this::replace);
    }

    public void match(@NotNull final Predicate match) {
        final Token key = parseValue(match.key(), match.key());
        if (matches.containsKey(key))
            throw new PredicateException("Duplicate match; Cannot override previously defined match");

        final Token value = parseValue(match.key(), match.value());
        matches.put(key, value);
    }

    public void replace(@NotNull final Predicate replace) {
        final Set<Token> keys = Predicate.parseItems(parseValue(replace.key(), replace.key()));
        if (keys.isEmpty())
            throw new PredicateException(replace.key(), "Could not find any items matching '" + replace.key().token() + "'");

        final Token value = parseValue(replace.key(), replace.value());

        for (final Token key : keys) {
            if (replacements.containsKey(key))
                throw new PredicateException(key, "Duplicate replacement; Cannot override previously defined replacement");

            replacements.put(key, value);
        }
    }

    @NotNull
    public Map<Token, Token> matches() {
        return Collections.unmodifiableMap(matches);
    }

    @NotNull
    public Map<Token, Token> replacements() {
        return Collections.unmodifiableMap(replacements);
    }
}
