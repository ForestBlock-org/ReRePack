package org.crayne.rerepack.syntax.parser;

import org.crayne.rerepack.syntax.parser.rule.token.TokenSpecification;
import org.jetbrains.annotations.NotNull;

public record ExpectedToken(@NotNull ExpressionIterator expectedIn, @NotNull TokenSpecification tokenSpecification) {

}
