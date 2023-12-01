package org.crayne.rerepack.syntax.ast;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.lexer.LexerSpecification;
import org.crayne.rerepack.syntax.parser.rule.token.Literal;
import org.crayne.rerepack.syntax.parser.rule.token.Special;
import org.crayne.rerepack.syntax.parser.rule.token.TokenSpecification;
import org.crayne.rerepack.syntax.parser.rule.token.TokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public enum NodeType {

    PARENT,
    SCOPE,
    EXPRESSION,
    IDENTIFIER("an identifier"),
    LITERAL,
    STRING_LITERAL("a string"),
    INTEGER_LITERAL("a valid 32-bit integer"),
    SPECIAL_CHARACTER;

    @Nullable
    private final String qualifiedIdentifier;

    NodeType() {
        this.qualifiedIdentifier = null;
    }

    NodeType(@NotNull final String qualifiedIdentifier) {
        this.qualifiedIdentifier = qualifiedIdentifier;
    }

    @NotNull
    public Optional<String> qualifiedIdentifier() {
        return Optional.ofNullable(qualifiedIdentifier);
    }

    @NotNull
    public static NodeType of(@NotNull final Token token, @NotNull final LexerSpecification specification) {
        return of(token.token(), specification);
    }

    @NotNull
    public static NodeType of(@NotNull final String token, @NotNull final LexerSpecification specification) {
        if (specification.isStringLiteral(token)) return STRING_LITERAL;
        if (specification.isIntegerLiteral(token)) return INTEGER_LITERAL;
        if (specification.isKeywordLiteral(token)) return LITERAL;

        final boolean special = specification.isAnySpecialToken(token);
        return special ? SPECIAL_CHARACTER : IDENTIFIER;
    }

    @NotNull
    public static TokenSpecification tokenSpecification(@NotNull final Token token,
                                                        @NotNull final LexerSpecification specification) {
        return tokenSpecification(token.token(), specification);
    }

    @NotNull
    public static TokenSpecification tokenSpecification(@NotNull final String token,
                                                        @NotNull final LexerSpecification specification) {

        final NodeType nodeType = of(token, specification);
        return switch (nodeType) {
            case IDENTIFIER, STRING_LITERAL, INTEGER_LITERAL -> new TokenType(nodeType);
            case LITERAL -> new Literal(token);
            case SPECIAL_CHARACTER -> new Special(token);
            default -> throw new UnsupportedOperationException("NodeType#of returned unexpected type");
        };
    }

}
