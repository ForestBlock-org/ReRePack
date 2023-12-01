package org.crayne.rerepack.workspace.util.replacement.predicate;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.workspace.util.except.DefinitionException;
import org.jetbrains.annotations.NotNull;

public class PredicateException extends DefinitionException {

    public PredicateException() {

    }

    public PredicateException(@NotNull final DefinitionException e) {
        super(e);
    }

    public PredicateException(@NotNull final String s) {
        super(s);
    }

    public PredicateException(@NotNull final Throwable t) {
        super(t);
    }

    public PredicateException(@NotNull final Token at) {
        super(at);
    }

    public PredicateException(@NotNull final Token at, @NotNull final String s) {
        super(at, s);
    }

    public PredicateException(@NotNull final Token at, @NotNull final Throwable t) {
        super(at, t);
    }
}
