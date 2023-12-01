package org.crayne.rerepack.syntax.parser.rule.token;

import org.crayne.rerepack.syntax.ast.NodeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TokenType implements TokenSpecification {

    @NotNull
    private final NodeType type;

    public TokenType(@NotNull final NodeType type) {
        this.type = type;
    }

    @NotNull
    public static TokenType token(@NotNull final NodeType type) {
        return new TokenType(type);
    }

    public boolean matches(@NotNull final TokenSpecification other) {
        return other.nodeType() == type;
    }

    @NotNull
    public Optional<String> asString() {
        return Optional.empty();
    }

    @NotNull
    public Optional<String> qualifiedIdentifier() {
        return type.qualifiedIdentifier();
    }

    @NotNull
    public NodeType nodeType() {
        return type;
    }

    public boolean equals(@Nullable final Object obj) {
        return obj instanceof final TokenSpecification spec && matches(spec);
    }

    @NotNull
    public String toString() {
        return "token(" +
                type.name().toLowerCase() +
                ')';
    }

}
