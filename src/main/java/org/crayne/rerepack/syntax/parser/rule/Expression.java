package org.crayne.rerepack.syntax.parser.rule;

import org.crayne.rerepack.syntax.parser.rule.token.TokenExpression;
import org.crayne.rerepack.syntax.parser.rule.token.TokenSpecification;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public interface Expression {

    @NotNull
    static Expression expr(@NotNull final List<TokenSpecification> expr) {
        return new TokenExpression(expr);
    }

    @NotNull
    static Expression expr(@NotNull final TokenSpecification @NotNull ... expr) {
        return expr(Arrays.stream(expr).toList());
    }

    @NotNull
    List<TokenSpecification> expected();

}
