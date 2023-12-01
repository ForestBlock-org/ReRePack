package org.crayne.rerepack.syntax.parser.rule.token;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.NodeType;
import org.crayne.rerepack.syntax.lexer.LexerSpecification;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface TokenSpecification {

    boolean matches(@NotNull final TokenSpecification other);

    default boolean matches(@NotNull final Token other, @NotNull final LexerSpecification lexerSpecification) {
        return matches(NodeType.tokenSpecification(other, lexerSpecification));
    }

    @NotNull
    Optional<String> asString();

    @NotNull
    Optional<String> qualifiedIdentifier();

    @NotNull
    NodeType nodeType();

}
