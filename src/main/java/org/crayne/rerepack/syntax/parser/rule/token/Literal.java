package org.crayne.rerepack.syntax.parser.rule.token;

import org.crayne.rerepack.syntax.ast.NodeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Literal implements TokenSpecification {

    @NotNull
    private final String literal;

    public Literal(@NotNull final String literal) {
        this.literal = literal;
    }

    @NotNull
    public static Literal literal(@NotNull final String literal) {
        return new Literal(literal);
    }

    public boolean matches(@NotNull final TokenSpecification other) {
        return other.asString().map(s -> s.equals(literal)).orElse(false);
    }

    @NotNull
    public Optional<String> asString() {
        return Optional.of(literal);
    }

    @NotNull
    public Optional<String> qualifiedIdentifier() {
        return Optional.of("'" + literal + "'");
    }

    @NotNull
    public NodeType nodeType() {
        return NodeType.LITERAL;
    }

    @NotNull
    public String toString() {
        return nodeType().name().toLowerCase() + "(\"" + literal + "\")";
    }

    public boolean equals(@Nullable final Object obj) {
        return obj instanceof final TokenSpecification spec && matches(spec);
    }

}
