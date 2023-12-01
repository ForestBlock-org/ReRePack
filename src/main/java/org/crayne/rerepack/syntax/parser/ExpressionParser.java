package org.crayne.rerepack.syntax.parser;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.syntax.ast.NodeType;
import org.crayne.rerepack.syntax.lexer.Lexer;
import org.crayne.rerepack.syntax.lexer.LexerSpecification;
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
    public Optional<Node> parse(@Nullable final File file, @NotNull final Collection<String> contentList, @NotNull final String content) {
        this.contentList = new ArrayList<>(contentList);
        return parse(lexer.tokenize(file, contentList, content));
    }

    @NotNull
    private Optional<Node> parse(@NotNull final List<Token> tokens) {
        final Node node = Node.of(NodeType.PARENT);
        final ParseResult result = parseScope(tokens, 0);
        result.resultLogMessage().ifPresent(logger::log);
        return result.error() ? Optional.empty() : Optional.of(node);
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

    public void removeUnexpectedTokenIterators(@NotNull final Set<ExpectedToken> expectedTokens, @NotNull final Token nextToken) {
        expectedTokens.stream()
                .filter(e -> !e.tokenSpecification().matches(nextToken, lexerSpecification))
                .map(ExpectedToken::expectedIn)
                .forEach(iterators::remove);
    }

    public boolean handleNextExpressionStart(@NotNull final List<Token> currentExpression) {
        final boolean startOfNewExpression = iterators.stream().noneMatch(ExpressionIterator::hasNext);

        if (!startOfNewExpression) return false;

        createIterators();
        System.out.println(currentExpression.stream().map(Token::token).toList());
        currentExpression.clear();

        return true;
    }

    @NotNull
    public ParseResult parseScope(@NotNull final List<Token> tokens, final int startIndex) {
        final List<Token> currentExpression = new ArrayList<>();

        for (int i = startIndex; i < tokens.size(); i++) {
            final Token nextToken = tokens.get(i);
            Set<ExpectedToken> expectedTokens = nextTokens();

            if (handleNextExpressionStart(currentExpression)) expectedTokens = nextTokens();
            removeUnexpectedTokenIterators(expectedTokens, nextToken);

            if (iterators.isEmpty()) return unexpectedToken(nextToken, expectedTokens);
            final Set<Scope> currentIteratorScopes = currentIteratorScopes();
            // TODO handle inner scopes recursively

            currentExpression.add(nextToken);
        }
        nextTokens();
        handleNextExpressionStart(currentExpression);
        return ParseResult.ok(tokens.size());
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
