package org.crayne.rerepack.syntax.parser.rule.token;

import org.crayne.rerepack.syntax.ast.NodeType;
import org.crayne.rerepack.syntax.lexer.LexerSpecification;
import org.crayne.rerepack.syntax.parser.rule.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TokenExpression implements Expression {

    @NotNull
    private final List<TokenSpecification> expected;

    public TokenExpression() {
        this.expected = new ArrayList<>();
    }

    public TokenExpression(@NotNull final List<TokenSpecification> expected) {
        this.expected = new ArrayList<>(expected);
    }

    public void validate(@NotNull final LexerSpecification specification) {
        expected.forEach(token -> {
            final Optional<String> asString = token.asString();
            if (asString.isEmpty()) return;

            final boolean literal = !(token instanceof Special);
            final boolean illegal = literal ?
                    !specification.isKeywordLiteral(asString.get()) :
                    !specification.isAnySpecialToken(asString.get());

            if (!illegal) return;

            throw new UnsupportedOperationException("Illegal "
                    + (literal ? "literal" : "special")
                    + " token was encountered in scope definition: " + asString.get());
        });
    }

    @NotNull
    public List<TokenSpecification> expected() {
        return Collections.unmodifiableList(expected);
    }

    @NotNull
    public TokenExpression literal(@NotNull final String literal) {
        this.expected.add(new Literal(literal));
        return this;
    }

    @NotNull
    public TokenExpression special(@NotNull final String special) {
        this.expected.add(new Special(special));
        return this;
    }

    @NotNull
    public TokenExpression tokenType(@NotNull final NodeType tokenType) {
        this.expected.add(new TokenType(tokenType));
        return this;
    }

    public boolean matches(@NotNull final Expression expression) {
        return expression instanceof final TokenExpression tokenExpression
                && tokenExpression.expected.equals(this.expected);
    }

    @NotNull
    public String toString() {
        return "TokenExpression{" +
                "expected=" + expected +
                '}';
    }

}
