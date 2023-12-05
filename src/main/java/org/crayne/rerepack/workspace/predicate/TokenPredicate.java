package org.crayne.rerepack.workspace.predicate;

import org.crayne.rerepack.syntax.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class TokenPredicate extends Predicate<Token> {

    public TokenPredicate(@NotNull final Token t1, @NotNull final Token t2) {
        super(t1, t2);
    }

    @NotNull
    public String toString() {
        return key() + " = \"" + value() + "\"";
    }

    @NotNull
    public TokenPredicate createCopy() {
        return new TokenPredicate(key(), value());
    }

    @NotNull
    public static Set<TokenPredicate> copyAll(@NotNull final Set<TokenPredicate> tokenPredicates) {
        return tokenPredicates.stream()
                .map(TokenPredicate::createCopy)
                .collect(Collectors.toSet());
    }

}
