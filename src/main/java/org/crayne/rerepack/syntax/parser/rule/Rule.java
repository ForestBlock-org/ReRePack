package org.crayne.rerepack.syntax.parser.rule;

import org.crayne.rerepack.syntax.lexer.LexerSpecification;
import org.crayne.rerepack.syntax.parser.rule.token.TokenExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rule {

    @NotNull
    private final List<Expression> expectedExpressions;

    @NotNull
    private final String name;

    public Rule(@NotNull final String name, @NotNull final List<Expression> expectedExpressions) {
        this.expectedExpressions = new ArrayList<>(expectedExpressions);
        this.name = name;
    }

    public void validate(@NotNull final LexerSpecification specification) {
        expectedExpressions.forEach(expr -> {
            if (expr instanceof final Scope scope) {
                scope.validate(specification);
                return;
            }
            if (!(expr instanceof final TokenExpression tokenExpression)) return;
            tokenExpression.validate(specification);
        });
    }

    @NotNull
    public static Rule rule(@NotNull final String name, @NotNull final List<Expression> expressions) {
        return new Rule(name, expressions);
    }

    @NotNull
    public static Rule rule(@NotNull final String name, @NotNull final Expression @NotNull ... expressions) {
        return new Rule(name, Arrays.stream(expressions).toList());
    }

    @NotNull
    public String name() {
        return name;
    }

    @NotNull
    public Rule expect(@NotNull final Expression expression) {
        this.expectedExpressions.add(expression);
        return this;
    }

    @NotNull
    public List<Expression> expectedExpressions() {
        return expectedExpressions;
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Rule rule = (Rule) o;

        if (!expectedExpressions.equals(rule.expectedExpressions)) return false;
        return name.equals(rule.name);
    }

    public int hashCode() {
        int result = expectedExpressions.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @NotNull
    public String toString() {
        return "rule(" + name + ")";
    }
}
