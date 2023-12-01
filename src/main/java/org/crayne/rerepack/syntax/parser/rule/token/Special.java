package org.crayne.rerepack.syntax.parser.rule.token;

import org.crayne.rerepack.syntax.ast.NodeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Special extends Literal {

    public Special(@NotNull final String specialCharacter) {
        super(specialCharacter);
    }

    @NotNull
    public static Special special(@NotNull final String special) {
        return new Special(special);
    }

    @NotNull
    public NodeType nodeType() {
        return NodeType.SPECIAL_CHARACTER;
    }

    public boolean equals(@Nullable final Object obj) {
        return obj instanceof final TokenSpecification spec && matches(spec);
    }

}
