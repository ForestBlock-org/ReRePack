package org.crayne.rerepack.workspace.parse;

import org.crayne.rerepack.syntax.parser.rule.Expression;
import org.crayne.rerepack.syntax.parser.rule.Scope;
import org.crayne.rerepack.syntax.parser.rule.token.Literal;
import org.crayne.rerepack.syntax.parser.rule.token.Special;
import org.crayne.rerepack.syntax.parser.rule.token.TokenType;
import org.crayne.rerepack.syntax.parser.ExpressionParser;
import org.crayne.rerepack.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

import static org.crayne.rerepack.syntax.ast.NodeType.IDENTIFIER;
import static org.crayne.rerepack.syntax.ast.NodeType.STRING_LITERAL;
import static org.crayne.rerepack.syntax.parser.rule.Expression.expr;
import static org.crayne.rerepack.syntax.parser.rule.Scope.parent;
import static org.crayne.rerepack.syntax.parser.rule.Scope.scope;
import static org.crayne.rerepack.syntax.parser.rule.token.Literal.literal;
import static org.crayne.rerepack.syntax.parser.rule.token.Special.special;
import static org.crayne.rerepack.syntax.parser.rule.token.TokenType.token;

public class RePackParserSpecification {

    @NotNull
    private final ExpressionParser parser;

    @NotNull
    public static RePackParserSpecification INSTANCE = new RePackParserSpecification(new Logger());

    @NotNull
    public ExpressionParser parser() {
        return parser;
    }

    @NotNull
    private static Scope createScope() {
        return scope(special("{"), special("}"));
    }

    @NotNull
    private static Scope createParameterScope() {
        return scope(special("("), special(")"));
    }

    @NotNull
    public static final String
            DEFINITION_STATEMENT = "definitionStatement",
            GLOBAL_DEFINITION_STATEMENT = "globalDefinitionStatement",
            MATCH_STATEMENT = "matchStatement",
            SINGLE_MATCH_EXPRESSION = "singleMatchExpression",
            ITEM_SINGLE_SET_PREDICATE = "itemSingleSetPredicate",
            ITEM_SINGLE_IDENTIFIER = "itemSingleIdentifier",
            ITEMS_STATEMENT_INDIVIDUAL = "itemsStatementIndividual",
            ITEMS_STATEMENT_SETALL = "itemsStatementSetall",
            WRITE_STATEMENT = "writeStatement",
            WRITE_STATEMENT_SINGLE_LINE = "writeStatementSingleLine",
            TEMPLATE_PARAM_SPEC_REQUIRED = "templateParamSpecRequired",
            TEMPLATE_PARAM_SPEC_WITH_DEFAULT = "templateParamSpecWithDefault",
            OPTIONAL_COMMA = "optionalComma",
            USE_PARAM_SPEC = "useParamSpec",
            TEMPLATE_STATEMENT = "templateStatement",
            USE_STATEMENT = "useStatement",
            CHAR_STATEMENT = "charStatement",
            CHAR_TYPE_SPEC_BITMAP = "charTypeSpecBitmap",
            CHAR_SINGLE_STATEMENT = "charSingleStatement",
            LANG_STATEMENT = "langStatement",
            LANG_SINGLE_EXPRESSION = "langSingleExpression",
            LANG_SINGLE_REPLACEMENT = "langSingleReplacement",
            COPY_STATEMENT = "copyStatement",
            COPY_STATEMENT_RAW = "copyStatementRaw";

    @NotNull
    private final Scope parentScopeDefinition;

    public RePackParserSpecification(@NotNull final Logger logger) {
        final Literal
                defKeyword = literal("def"),
                globalKeyword = literal("global"),

                templateKeyword = literal("template"),
                requireKeyword = literal("require"),

                matchKeyword = literal("match"),
                replaceKeyword = literal("replace"),
                itemsKeyword = literal("items"),

                writeKeyword = literal("write"),
                copyKeyword = literal("copy"),
                rawKeyword = literal("raw"),

                charKeyword = literal("char"),

                langKeyword = literal("lang");

        final TokenType
                identifier = token(IDENTIFIER),
                stringLiteral = token(STRING_LITERAL);

        final Special
                equalsSign = special("="),
                commaSign = special(","),
                arrow = special("=>");

        final Expression
                definitionStatement = expr(defKeyword, identifier, equalsSign, stringLiteral),
                globalDefinitionStatement = expr(globalKeyword, identifier, equalsSign, stringLiteral),
                singleMatchExpression = expr(stringLiteral, equalsSign, stringLiteral),
                itemSingleSetPredicate = expr(stringLiteral, equalsSign, stringLiteral),
                itemSingleIdentifier = expr(stringLiteral),
                itemSetAllSuffix = expr(equalsSign, stringLiteral),
                itemPrefix = expr(itemsKeyword),
                replacePrefix = expr(replaceKeyword),
                matchPrefix = expr(matchKeyword),
                writePrefix = expr(writeKeyword, stringLiteral),
                singleWriteLine = expr(stringLiteral),
                templatePrefix = expr(templateKeyword, identifier),
                useStatement = expr(identifier),
                templateParam = expr(requireKeyword, identifier),
                templateParamWithDefault = expr(identifier, equalsSign, stringLiteral),
                useParam = expr(identifier, equalsSign, stringLiteral),
                optionalComma = expr(commaSign),
                charStatementPrefix = expr(charKeyword, stringLiteral, stringLiteral),
                charSingleStatement = expr(stringLiteral, equalsSign, stringLiteral),
                langSingleExpression = expr(stringLiteral),
                langSingleReplacement = expr(stringLiteral, equalsSign, stringLiteral),
                langPrefix = expr(langKeyword),
                copyStatement = expr(copyKeyword, stringLiteral, arrow, stringLiteral),
                copyStatementRaw = expr(rawKeyword, copyKeyword, stringLiteral, arrow, stringLiteral);

        final Scope
                matchScope = createScope().rule(SINGLE_MATCH_EXPRESSION, singleMatchExpression),
                individualItemScope = createScope().rule(ITEM_SINGLE_SET_PREDICATE, itemSingleSetPredicate),
                setallItemScope = createScope().rule(ITEM_SINGLE_IDENTIFIER, itemSingleIdentifier),
                replaceScope = createScope()
                        .rule(ITEMS_STATEMENT_INDIVIDUAL, itemPrefix, individualItemScope)
                        .rule(ITEMS_STATEMENT_SETALL, itemPrefix, setallItemScope, itemSetAllSuffix),
                writeScope = createScope()
                        .rule(WRITE_STATEMENT_SINGLE_LINE, singleWriteLine),
                templateParamScope = createParameterScope()
                        .rule(TEMPLATE_PARAM_SPEC_REQUIRED, templateParam)
                        .rule(TEMPLATE_PARAM_SPEC_WITH_DEFAULT, templateParamWithDefault)
                        .rule(OPTIONAL_COMMA, optionalComma),
                useParamScope = createParameterScope()
                        .rule(USE_PARAM_SPEC, useParam)
                        .rule(OPTIONAL_COMMA, optionalComma),

                langScope = createScope().rule(LANG_SINGLE_EXPRESSION, langSingleExpression),
                langReplaceScope = createScope()
                        .rule(LANG_SINGLE_REPLACEMENT, langSingleReplacement),

                charScope = createScope()
                        .rule(CHAR_SINGLE_STATEMENT, charSingleStatement),

                templateScope = createScope()
                        .rule(DEFINITION_STATEMENT, definitionStatement)
                        .rule(MATCH_STATEMENT, matchPrefix, matchScope, replacePrefix, replaceScope)
                        .rule(WRITE_STATEMENT, writePrefix, writeScope)
                        .rule(CHAR_STATEMENT, charStatementPrefix, charScope)
                        .rule(LANG_STATEMENT, langPrefix, langScope, replacePrefix, langReplaceScope)
                        .rule(USE_STATEMENT, useStatement, useParamScope)
                        .rule(COPY_STATEMENT, copyStatement)
                        .rule(COPY_STATEMENT_RAW, copyStatementRaw),

                parentScope = parent()
                        .rule(DEFINITION_STATEMENT, definitionStatement)
                        .rule(GLOBAL_DEFINITION_STATEMENT, globalDefinitionStatement)
                        .rule(MATCH_STATEMENT, matchPrefix, matchScope, replacePrefix, replaceScope)
                        .rule(WRITE_STATEMENT, writePrefix, writeScope)
                        .rule(TEMPLATE_STATEMENT, templatePrefix, templateParamScope, templateScope)
                        .rule(USE_STATEMENT, useStatement, useParamScope)
                        .rule(CHAR_STATEMENT, charStatementPrefix, charScope)
                        .rule(LANG_STATEMENT, langPrefix, langScope, replacePrefix, langReplaceScope)
                        .rule(COPY_STATEMENT, copyStatement)
                        .rule(COPY_STATEMENT_RAW, copyStatementRaw);

        this.parentScopeDefinition = parentScope;
        this.parser = new ExpressionParser(parentScope, logger, RePackLexerSpecification.INSTANCE);
    }

    @NotNull
    public Scope parentScopeDefinition() {
        return parentScopeDefinition;
    }
}
