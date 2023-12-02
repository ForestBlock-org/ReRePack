package org.crayne.rerepack.syntax.parser;

import org.apache.commons.lang3.tuple.Pair;
import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.syntax.ast.NodeType;
import org.crayne.rerepack.syntax.lexer.Lexer;
import org.crayne.rerepack.syntax.lexer.LexerSpecification;
import org.crayne.rerepack.syntax.parser.except.ParserException;
import org.crayne.rerepack.syntax.parser.result.ErrorResult;
import org.crayne.rerepack.syntax.parser.result.ParseResult;
import org.crayne.rerepack.syntax.parser.rule.Scope;
import org.crayne.rerepack.syntax.parser.rule.token.TokenSpecification;
import org.crayne.rerepack.util.logging.Logger;
import org.crayne.rerepack.util.logging.LoggingLevel;
import org.crayne.rerepack.util.logging.message.TraceBackMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ExpressionParser {

    @NotNull
    private final Set<ExpressionIterator> iterators;

    @NotNull
    private final Scope parentScope;

    @NotNull
    private final LexerSpecification lexerSpecification;

    @NotNull
    private final Lexer lexer;

    @NotNull
    private final Logger logger;

    @Nullable
    private List<String> contentList;

    public ExpressionParser(@NotNull final Scope parentScope,
                            @NotNull final Logger logger,
                            @NotNull final LexerSpecification lexerSpecification) {
        this.parentScope = parentScope;
        this.iterators = new HashSet<>();
        this.lexerSpecification = lexerSpecification;
        this.logger = logger;
        this.lexer = new Lexer(logger, lexerSpecification);
        createIterators();
    }

    @NotNull
    public Optional<Node> parse(@NotNull final File file, @Nullable final List<String> addContent) throws IOException {
        return parse(file, Files.readString(file.toPath()), addContent);
    }

    @NotNull
    public Optional<Node> parse(@Nullable final File file, @NotNull final String content, @Nullable final List<String> addContent) {
        final List<String> contentList = Arrays.stream(content.split("\n")).toList();
        if (addContent != null) addContent.addAll(contentList);
        return parse(file, contentList, content);
    }

    @NotNull
    public Optional<Node> parse(@Nullable final File file, @NotNull final List<String> contentList, @NotNull final String content) {
        return parse(lexer.tokenize(file, contentList, content), contentList);
    }

    @NotNull
    public Optional<Node> parse(@NotNull final List<Token> tokens, @NotNull final List<String> contentList) {
        final ParseResult result = parseScope(tokens, 0, contentList, true);
        result.resultLogMessage().ifPresent(logger::log);
        return result.node();
    }

    public void createIterators() {
        iterators.clear();
        iterators.addAll(parentScope.rules()
                .stream()
                .map(ExpressionIterator::new)
                .collect(Collectors.toSet()));
    }

    @NotNull
    public Set<ExpectedToken> nextTokens() {
        return ExpressionIterator.expectedNextTokens(iterators);
    }

    @NotNull
    public Set<Scope> currentIteratorScopes() {
        return iterators.stream()
                .map(ExpressionIterator::currentExpressionAsScope)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    @NotNull
    public ErrorResult parserError(@NotNull final String message, @NotNull final Token at,
                                   @NotNull final String @NotNull ... hints) {
        if (contentList == null)
            throw new UnsupportedOperationException("Cannot invoke parser error without file content information");

        return ParseResult.error(TraceBackMessage.Builder
                .createBuilder(message, LoggingLevel.PARSING_ERROR)
                .positionInformation(at, contentList)
                .hints(hints)
                .build());
    }

    @NotNull
    public ParseResult parseScope(@NotNull final List<Token> tokens, final int startIndex,
                                  @NotNull final List<String> contentList, final boolean parent) {
        this.contentList = new ArrayList<>(contentList);
        final Node scopeNode = parent ? Node.of(NodeType.PARENT) : Node.of(NodeType.SCOPE);

        Node expressionNode = Node.of(NodeType.EXPRESSION);

        boolean handleDuplicateEndOfScope = false;
        for (int i = startIndex; i < tokens.size(); i++) {
            final Token nextToken = tokens.get(i);
            final boolean endOfScope = checkScope(nextToken, parentScope, Scope::end);

            Set<ExpectedToken> expectedTokens = nextTokens();

            if (handleNextExpressionStart(expressionNode)) {
                addExpressionToScope(scopeNode, expressionNode);
                expectedTokens = nextTokens();
                expressionNode = Node.of(NodeType.EXPRESSION);
            }
            removeUnexpectedTokenIterators(expectedTokens, expressionNode, nextToken);

            if (endOfScope && !handleDuplicateEndOfScope) {
                nextTokens();
                addExpressionToScopeAtNextStart(scopeNode, expressionNode);
                return ParseResult.ok(i - 1, scopeNode);
            }
            if (iterators.isEmpty()) return unexpectedToken(nextToken, expectedTokens);

            final Optional<ParseResult> scopeParseResult = handleScopeBeginning(nextToken, tokens, i, contentList);

            if (scopeParseResult.isPresent()) {
                final ParseResult result = scopeParseResult.get();
                if (result.error()) return result;

                i = result.nextIndex();
                handleDuplicateEndOfScope = true;
                addTokenToExpression(expressionNode, nextToken);
                expressionNode.addChildren(result.node().orElseThrow());
                continue;
            }
            handleDuplicateEndOfScope = false;
            addTokenToExpression(expressionNode, nextToken);
        }
        nextTokens();
        addExpressionToScopeAtNextStart(scopeNode, expressionNode);
        return ParseResult.ok(tokens.size(), scopeNode);
    }
    private void removeUnexpectedTokenIterators(@NotNull final Set<ExpectedToken> expectedTokens,
                                                @NotNull final Node expressionNode,
                                                @NotNull final Token nextToken) {
        expectedTokens.stream()
                .filter(e -> !e.tokenSpecification().matches(nextToken, lexerSpecification))
                .map(ExpectedToken::expectedIn)
                .forEach(iterators::remove);

        iterators.removeIf(it -> !it.hasNext());
        expressionNodeName(expressionNode);
    }

    private void expressionNodeName(@NotNull final Node expressionNode) {
        if (iterators.size() != 1) return;

        expressionNode.name(iterators.stream()
                .findAny()
                .orElseThrow()
                .rule()
                .name());
    }

    private boolean handleNextExpressionStart(@NotNull final Node expressionNode) {
        final boolean startOfNewExpression = iterators.stream().noneMatch(ExpressionIterator::hasNext);
        if (!startOfNewExpression) return false;

        expressionNodeName(expressionNode);
        createIterators();
        return true;
    }

    private void addExpressionToScope(@NotNull final Node scopeNode, @NotNull final Node expressionNode) {
        if (!expressionNode.children().isEmpty()) scopeNode.addChildren(expressionNode);
    }

    private void addTokenToExpression(@NotNull final Node expressionNode, @NotNull final Token nextToken) {
        expressionNode.addChildren(new Node(nextToken, lexerSpecification));
    }

    private void addExpressionToScopeAtNextStart(@NotNull final Node scopeNode, @NotNull final Node expressionNode) {
        if (handleNextExpressionStart(expressionNode)) addExpressionToScope(scopeNode, expressionNode);
    }

    private void handleAmbiguousResult(@NotNull final Set<ParseResult> successfulScopeParses,
                                       @NotNull final ParseResult result) {
        if (successfulScopeParses.stream().anyMatch(r -> !r.equals(result)))
            throw new ParserException("Ambiguous scope rule definitions, " +
                    "cannot find scope end token index " + successfulScopeParses);
    }

    private boolean errorScope(@NotNull final Set<Pair<ExpressionParser, ParseResult>> scopeParsingResults,
                                       @NotNull final Scope currentScope) {
        return scopeParsingResults.stream()
                .anyMatch(p -> p.getRight().error() && p.getLeft().parentScope == currentScope);
    }

    private boolean errorScopeIterator(@NotNull final Set<Pair<ExpressionParser, ParseResult>> scopeParsingResults,
                                       @NotNull final ExpressionIterator iterator) {
        return iterator.currentExpressionAsScope()
                .map(currentScope -> errorScope(scopeParsingResults, currentScope))
                .orElse(false);
    }

    @NotNull
    private Set<ExpressionParser> createScopeParsers(@NotNull final Set<Scope> startOfScope) {
        return startOfScope.stream()
                .map(s -> new ExpressionParser(s, logger, lexerSpecification))
                .collect(Collectors.toSet());
    }

    @NotNull
    private Set<Pair<ExpressionParser, ParseResult>> parseScopes(@NotNull final Set<ExpressionParser> parsers,
                                                                 @NotNull final List<Token> tokens,
                                                                 final int currentIndex,
                                                                 @NotNull final List<String> contentList) {
        return parsers.stream()
                .map(e -> Pair.of(e, e.parseScope(
                        tokens, currentIndex + 1,
                        contentList, false))
                )
                .collect(Collectors.toSet());
    }

    @NotNull
    private Optional<ParseResult> handleScopeBeginning(@NotNull final Token nextToken,
                                                       @NotNull final List<Token> tokens,
                                                       final int currentIndex,
                                                       @NotNull final List<String> contentList) {
        final Set<Scope> startOfScope = filterScopes(nextToken, currentIteratorScopes(), Scope::begin);

        final Set<Pair<ExpressionParser, ParseResult>> scopeParsingResults
                = parseScopes(createScopeParsers(startOfScope), tokens, currentIndex, contentList);

        iterators.removeIf(it -> errorScopeIterator(scopeParsingResults, it));

        final Set<ParseResult> successfulScopeParses = filterScopeResults(scopeParsingResults, ParseResult::ok);
        final Set<ParseResult> errorScopeParses = filterScopeResults(scopeParsingResults, ParseResult::error);

        if (successfulScopeParses.isEmpty() && !errorScopeParses.isEmpty())
            return errorScopeParses.stream().findAny();

        return successfulScopeParses.stream()
                .findAny()
                .stream()
                .peek(result -> handleAmbiguousResult(successfulScopeParses, result))
                .findAny();
    }

    @NotNull
    private Set<ParseResult> filterScopeResults(@NotNull final Set<Pair<ExpressionParser, ParseResult>> scopeResults,
                                                @NotNull final Predicate<ParseResult> filter) {
        return scopeResults.stream()
                .map(Pair::getRight)
                .filter(filter)
                .collect(Collectors.toSet());
    }

    @NotNull
    private Set<Scope> filterScopes(@NotNull final Token nextToken,
                                         @NotNull final Set<Scope> currentIteratorScopes,
                                         @NotNull final Function<Scope, Optional<TokenSpecification>> scopeTokenFunction) {
        if (currentIteratorScopes.isEmpty()) return new HashSet<>();

        return currentIteratorScopes.stream()
                .filter(s -> checkScope(nextToken, s, scopeTokenFunction))
                .collect(Collectors.toSet());
    }

    private boolean checkScope(@NotNull final Token nextToken,
                                   @NotNull final Scope s,
                                   @NotNull final Function<Scope, Optional<TokenSpecification>> scopeTokenFunction) {
        return scopeTokenFunction.apply(s)
                        .map(t -> t.matches(nextToken, lexerSpecification))
                        .orElse(false);
    }

    @NotNull
    private ErrorResult unexpectedToken(@NotNull final Token token, @NotNull final Set<ExpectedToken> expectedTokens) {
        final List<String> possibleCandidates = expectedTokens.stream()
                .map(ExpectedToken::tokenSpecification)
                .map(TokenSpecification::qualifiedIdentifier)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (possibleCandidates.isEmpty()) return parserError("Unexpected token '" + token.token() + "'", token);

        if (possibleCandidates.size() == 1) {
            final String candidate = possibleCandidates.get(0);
            return parserError("Unexpected token '" + token.token() + "'", token,
                    "Expected token: " + candidate,
                    "Replace '" + token.token() + "' with " + candidate + " to fix this issue.", "");
        }
        return parserError("Unexpected token '" + token.token() + "'", token,
                "Expected possible token(s):", String.join(", ", possibleCandidates),
                "Replace '" + token.token() + "' with any of the above candidates to fix this issue.", "");
    }

}
