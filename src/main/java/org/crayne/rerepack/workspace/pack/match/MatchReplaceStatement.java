package org.crayne.rerepack.workspace.pack.match;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.util.minecraft.VanillaItem;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.crayne.rerepack.workspace.predicate.TokenPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.*;

public class MatchReplaceStatement implements Parseable {

    @NotNull
    private final Set<TokenPredicate> matches, replacements;

    public MatchReplaceStatement(@NotNull final Set<TokenPredicate> matches,
                                 @NotNull final Set<TokenPredicate> replacements) {
        this.matches = new HashSet<>(matches);
        this.replacements = new HashSet<>(replacements);
    }

    public MatchReplaceStatement() {
        this.matches = new HashSet<>();
        this.replacements = new HashSet<>();
    }

    @NotNull
    public static Set<Token> parseItems(@NotNull final Token key) {
        final String keyValue = key.toString();
        final boolean moddedItem = VanillaItem.moddedItem(keyValue);
        if (moddedItem) return Collections.singleton(key);

        return VanillaItem.allMatching(keyValue)
                .stream()
                .map(s -> Token.of(s, key))
                .collect(Collectors.toSet());
    }

    public void parseFromAST(@NotNull final Node ast) throws WorkspaceException {
        final Set<TokenPredicate> matches = parseMatches(ast.child(2));
        final Set<TokenPredicate> replacements = parseReplacements(ast.child(6));

        for (final TokenPredicate match : matches) match(match);
        for (final TokenPredicate replace : replacements) replace(replace);
    }

    public void match(@NotNull final TokenPredicate match) throws WorkspaceException {
        if (matches.contains(match))
            throw new WorkspaceException("Duplicate match " + match);

        matches.add(match);
    }

    public void replace(@NotNull final TokenPredicate replace) throws WorkspaceException {
        if (replacements.contains(replace))
            throw new WorkspaceException("Duplicate replacement " + replace);

        replacements.add(replace);
    }

    @NotNull
    public Set<TokenPredicate> matches() {
        return Collections.unmodifiableSet(matches);
    }

    @NotNull
    public Set<TokenPredicate> replacements() {
        return Collections.unmodifiableSet(replacements);
    }

    @NotNull
    private static TokenPredicate parseDefaultPredicate(@NotNull final Node node) {
        final Token key = node.child(0).valueClean();
        final Token value = node.child(2).valueClean();

        return new TokenPredicate(key, value);
    }

    @NotNull
    private static Set<TokenPredicate> parseIndividualItemPredicates(@NotNull final Node replacementExpressions) {
        return replacementExpressions.children(ITEMS_STATEMENT_INDIVIDUAL)
                .stream()
                .map(n -> n.child(2))
                .map(MatchReplaceStatement::parseSingleItemSetPredicate)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Set<TokenPredicate> parseSingleItemSetPredicate(@NotNull final Node node) {
        return node.children(ITEM_SINGLE_SET_PREDICATE)
                .stream()
                .map(MatchReplaceStatement::parseDefaultPredicate)
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Set<TokenPredicate> parseSetallItemPredicates(@NotNull final Node replacementExpressions) {
        return replacementExpressions.children(ITEMS_STATEMENT_SETALL)
                .stream()
                .map(n -> {
                    final Token value = n.child(5).valueClean();
                    return parseSingleItemIdentifier(n.child(2), value);
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Set<TokenPredicate> parseSingleItemIdentifier(@NotNull final Node node,
                                                                 @NotNull final Token value) {
        return node.children(ITEM_SINGLE_IDENTIFIER)
                .stream()
                .map(singleNode -> new TokenPredicate(singleNode.child(0).valueClean(), value))
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Set<TokenPredicate> parseMatches(@NotNull final Node node) {
        final List<Node> matchExpressions = node.children(SINGLE_MATCH_EXPRESSION);

        return matchExpressions.stream()
                .map(MatchReplaceStatement::parseDefaultPredicate)
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Set<TokenPredicate> parseReplacements(@NotNull final Node node) {
        final Set<TokenPredicate> replacements = new HashSet<>();

        replacements.addAll(parseIndividualItemPredicates(node));
        replacements.addAll(parseSetallItemPredicates(node));
        return replacements;
    }

}
