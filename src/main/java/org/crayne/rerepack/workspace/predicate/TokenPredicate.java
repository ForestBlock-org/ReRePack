package org.crayne.rerepack.workspace.predicate;

import org.crayne.rerepack.syntax.Token;
import org.jetbrains.annotations.NotNull;

public class TokenPredicate extends Predicate<Token> {

    public TokenPredicate(@NotNull final Token t1, @NotNull final Token t2) {
        super(t1, t2);
    }

    @NotNull
    public String toString() {
        return key() + " = \"" + value() + "\"";
    }

}
