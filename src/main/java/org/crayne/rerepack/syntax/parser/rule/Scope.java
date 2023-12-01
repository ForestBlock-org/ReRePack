package org.crayne.rerepack.syntax.parser.rule;

import org.crayne.rerepack.syntax.lexer.LexerSpecification;
import org.crayne.rerepack.syntax.parser.rule.token.TokenSpecification;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Scope implements Expression {

    @Nullable
    private final TokenSpecification begin, end;

    @NotNull
    private final Set<Rule> rules;

    public Scope(@NotNull final TokenSpecification begin, @NotNull final TokenSpecification end,
                 @NotNull final Set<Rule> rules) {
        this.begin = begin;
        this.end = end;
        this.rules = new HashSet<>(rules);
    }

    public Scope(@NotNull final TokenSpecification begin, @NotNull final TokenSpecification end) {
        this(begin, end, new HashSet<>());
    }

    public Scope(@NotNull final Set<Rule> rules) {
        this.begin = null;
        this.end = null;
        this.rules = new HashSet<>(rules);
    }

    public Scope() {
        this(new HashSet<>());
    }

    @NotNull
    public List<TokenSpecification> expected() {
        if (begin == null || end == null) return List.of();
        return List.of(begin, end);
    }

    public void validate(@NotNull final LexerSpecification specification) {
        for (final Rule rule : rules) {
            rule.validate(specification);
        }
    }

    @NotNull
    public static Scope parent() {
        return new Scope();
    }

    @NotNull
    public static Scope scope(@NotNull final TokenSpecification begin,
                              @NotNull final TokenSpecification end) {
        return new Scope(begin, end);
    }

    @NotNull
    public Scope rule(@NotNull final Rule rule) {
        this.rules.add(rule);
        return this;
    }

    @NotNull
    public Scope rule(@NotNull final String name, @NotNull final List<Expression> expectedExpressions) {
        this.rules.add(Rule.rule(name, expectedExpressions));
        return this;
    }

    @NotNull
    public Scope rule(@NotNull final String name, @NotNull final Expression @NotNull ... expectedExpressions) {
        this.rules.add(Rule.rule(name, expectedExpressions));
        return this;
    }

    @NotNull
    public Set<Rule> rules() {
        return Collections.unmodifiableSet(rules);
    }

    @NotNull
    public Optional<TokenSpecification> begin() {
        return Optional.ofNullable(begin);
    }

    @NotNull
    public Optional<TokenSpecification> end() {
        return Optional.ofNullable(end);
    }

    @NotNull
    public String toString() {
        return "Scope{" +
                "begin=" + begin +
                ", end=" + end +
                ", rules=" + rules +
                '}';
    }
}
