package org.crayne.rerepack.syntax.parser;

import org.crayne.rerepack.syntax.parser.rule.Expression;
import org.crayne.rerepack.syntax.parser.rule.Rule;
import org.crayne.rerepack.syntax.parser.rule.Scope;
import org.crayne.rerepack.syntax.parser.rule.token.TokenSpecification;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ExpressionIterator implements Iterator<TokenSpecification> {

    @NotNull
    private final Rule rule;

    @NotNull
    private final List<Expression> expressions;

    private int expressionIndex, tokenIndex;

    public ExpressionIterator(@NotNull final Rule rule) {
        this.rule = rule;
        this.expressions = rule.expectedExpressions();
        this.expressionIndex = 0;
        this.tokenIndex = 0;
    }

    @NotNull
    public Rule rule() {
        return rule;
    }

    public boolean hasNext() {
        return expressionIndex < expressions.size();
    }

    @Nullable
    public TokenSpecification next() {
        if (!hasNext()) return null;

        final Expression expr = expressions.get(expressionIndex);
        final List<TokenSpecification> tokens = expr.expected();

        if (tokenIndex >= tokens.size()) {
            tokenIndex = 0;
            expressionIndex++;
            return next();
        }
        final TokenSpecification tokenSpec = tokens.get(tokenIndex);
        tokenIndex++;
        return tokenSpec;
    }

    @Nullable
    public ExpectedToken nextExpected() {
        return Optional.ofNullable(next())
                .map(t -> new ExpectedToken(this, t))
                .orElse(null);
    }

    @NotNull
    public static Set<ExpectedToken> expectedNextTokens(@NotNull final Set<ExpressionIterator> iterators) {
        return iterators.stream()
                .map(ExpressionIterator::nextExpected)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @NotNull
    public Optional<Scope> currentExpressionAsScope() {
        if (!hasNext()) return Optional.empty();

        return Optional.ofNullable(expressions.get(expressionIndex))
                .filter(e -> e instanceof Scope)
                .map(e -> (Scope) e);
    }

    @NotNull
    public String toString() {
        return "ExpressionIterator{" +
                "rule=" + rule +
                '}';
    }

}
