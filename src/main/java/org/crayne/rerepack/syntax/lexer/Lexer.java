package org.crayne.rerepack.syntax.lexer;

import org.apache.commons.text.StringEscapeUtils;
import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.NodeType;
import org.crayne.rerepack.util.logging.Logger;
import org.crayne.rerepack.util.logging.LoggingLevel;
import org.crayne.rerepack.util.logging.message.TraceBackMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Lexer {

    @NotNull
    private final LexerSpecification specification;

    @NotNull
    private final Logger logger;


    @NotNull
    private final List<Token> result = new ArrayList<>();
    private char currentQuotes = 0;

    @Nullable
    private Token beganString;

    @Nullable
    private String previous = null;

    private boolean singleLineCommented = false;
    private boolean multilineCommented = false;

    @NotNull
    private StringBuilder currentToken = new StringBuilder();

    private int line = 1;
    private int column = 0;

    @Nullable
    private File currentFile;

    @Nullable
    private List<String> currentFileContent;

    private char atPos = 0;
    private boolean encounteredError = false;
    private boolean unfinishedTextLiteral = false;

    public Lexer(@NotNull final Logger logger, @NotNull final LexerSpecification specification) {
        this.specification = specification;
        this.logger = logger;
    }

    @NotNull
    public List<Token> tokenize(@NotNull final File file) throws IOException {
        this.currentFile = file;
        return tokenize(file, Files.readString(file.toPath()));
    }

    @NotNull
    private List<Token> tokenize(@NotNull final File file, @NotNull final String content) {
        this.currentFile = file;
        return tokenize(content);
    }

    @NotNull
    public List<Token> tokenize(@NotNull final String content) {
        return tokenize(null, Arrays.stream(content.split("\n")).toList(), content);
    }

    @NotNull
    public List<Token> tokenize(@Nullable final File file, @NotNull final Collection<String> contentList, @NotNull final String content) {
        this.currentFile = file;
        result.clear();
        currentFileContent = new ArrayList<>(contentList);

        final String contentEOF = content + "\n";
        currentFileContent.add("");

        for (int i = 0; i < contentEOF.length(); i++) {
            this.atPos = contentEOF.charAt(i);
            column++;
            if (encounteredError) {
                reset();
                return new ArrayList<>();
            }

            if (handleQuoted() || handleWhitespaces() || handleSpecialTokens()) continue;
            if (notInComment()) currentToken.append(atPos);
        }
        final boolean quoted = handleQuoted();
        final boolean whitespaces = !quoted && handleWhitespaces();
        if (!whitespaces && !specification.validStringLiteralChar(currentToken.toString())) handleSpecialTokens();

        reset();
        return result;
    }

    private void lexerError(@SuppressWarnings("SameParameterValue") @NotNull final String message,
                            @NotNull final Token at, @NotNull final String... help) {

        encounteredError = true;
        if (currentFileContent == null) {
            logger.log(TraceBackMessage.Builder
                    .createBuilder(message, LoggingLevel.LEXING_ERROR)
                    .at(at)
                    .lineInCode("")
                    .hints(help)
                    .build());
            return;
        }
        logger.log(TraceBackMessage.Builder
                .createBuilder(message, LoggingLevel.LEXING_ERROR)
                .positionInformation(at, currentFileContent)
                .hints(help)
                .build());
    }

    private Token tokenOf(@NotNull final String token) {
        return new Token(token, line, Math.max(column - token.length(), 0), currentFile);
    }

    private Token currentToken() {
        return tokenOf(currentToken.toString());
    }

    private boolean notInComment() {
        return !singleLineCommented && !multilineCommented;
    }

    private boolean appendToCurrentString() {
        if (currentQuotes != 0) {
            currentToken.append(atPos);
            previous = "" + atPos;
            return true;
        }
        return false;
    }

    private boolean isPreviousEscape() {
        return previous != null && previous.equals("\\");
    }

    private void beginString() {
        beganString = currentToken();
        addCurrent();
        current(atPos + "");
        currentQuotes = atPos;
    }

    private boolean endString() {
        if (currentQuotes != atPos) return false;
        currentToken.append(atPos);

        final String str = !specification.charactersEscaped(currentToken.toString())
                ? currentToken.toString()
                : StringEscapeUtils.unescapeJava(currentToken.toString());

        current(str);
        addCurrent();
        clearCurrent();
        currentQuotes = 0;
        return true;
    }

    private boolean beginOrEndString() {
        if (currentQuotes == 0) {
            beginString();
            return true;
        }
        if (!specification.validStringLiteralChar(currentQuotes)) return false;
        if (endString()) return true;

        return appendToCurrentString();
    }

    private boolean handleQuoted() {
        if (!notInComment()) return false;
        if (currentQuotes != 0) handleNewlines();
        if (!specification.validStringLiteralChar(atPos)) return appendToCurrentString();

        return (isPreviousEscape() && appendToCurrentString()) || beginOrEndString();
    }

    private void handleNewlines() {
        if (atPos != '\n') return;

        if (currentQuotes != 0 && !unfinishedTextLiteral) {
            assert beganString != null;
            lexerError("Expected text literal to end at the same line", beganString,
                    "String literals must begin and end with the same character.");
            unfinishedTextLiteral = true;
        }
        column = 0;
        line++;
        singleLineCommented = false;
    }

    private boolean handleWhitespaces() {
        if (!Character.isWhitespace(atPos)) return false;

        if (!currentToken.isEmpty()) {
            addCurrent();
            clearCurrent();
        }
        handleNewlines();
        return true;
    }

    private boolean handleSingleLineCommentBegin(@NotNull final String extendedSpecialChar) {
        if (!specification.singleLineCommentBegin().equals(extendedSpecialChar)) return false;

        singleLineCommented = true;
        return true;
    }

    private boolean handleMultiLineCommentBegin(@NotNull final String extendedSpecialChar) {
        if (!specification.multiLineCommentBegin().equals(extendedSpecialChar)) return false;

        multilineCommented = true;
        return true;
    }

    private boolean handleMultiLineCommentEnd() {
        if (!(previous + atPos).equals(specification.multiLineCommentEnd()) || !multilineCommented) return false;

        multilineCommented = false;
        return true;
    }

    private boolean handleComments(@NotNull final String extendedSpecialChar) {
        if (handleSingleLineCommentBegin(extendedSpecialChar)
                || handleMultiLineCommentBegin(extendedSpecialChar)
                || handleMultiLineCommentEnd()) {
            clearCurrent();
            return true;
        }
        return false;
    }

    private boolean currentExtendedSpecial() {
        return specification.isExtendedSpecialCharacter(currentToken.toString());
    }

    private boolean currentNotBlank() {
        return !currentToken.toString().isBlank();
    }

    private void current(@NotNull final String string) {
        currentToken = new StringBuilder(string);
    }

    private void clearCurrent() {
        current("");
    }

    private void addCurrent() {
        if (currentToken.isEmpty()) return;
        result.add(currentToken());
        previous = currentToken.toString();
    }

    private boolean addCurrentExtended() {
        if (notInComment() && currentNotBlank() && currentExtendedSpecial()) {
            addCurrent();
            current("" + atPos);
            return true;
        }
        return false;
    }

    private boolean validExtendedChar(@NotNull final String extendedSpecialChar) {
        return specification.extendedSpecialCharacters().stream().anyMatch(s -> s.startsWith(extendedSpecialChar));
    }

    private boolean handleSpecialTokens() {
        if (!specification.isSpecialToken(atPos + "")) return addCurrentExtended();

        final String extendedSpecialChar = currentToken.toString() + atPos;
        if (handleComments(extendedSpecialChar)) return true;

        if (!notInComment()) {
            previous = "" + atPos;
            return true;
        }
        if (currentExtendedSpecial() && validExtendedChar(extendedSpecialChar)
                && NodeType.of(extendedSpecialChar, specification) != NodeType.LITERAL) {

            currentToken.append(atPos);
            return true;
        }
        if (currentNotBlank()) addCurrent();

        current("" + atPos);
        return true;
    }

    public void reset() {
        this.currentFileContent = new ArrayList<>();
        this.column = 0;
        this.line = 1;
        this.currentFile = null;
        this.encounteredError = false;
        this.beganString = null;
        this.atPos = 0;
        this.currentQuotes = 0;
        this.currentToken = new StringBuilder();
        this.previous = null;
        this.singleLineCommented = false;
        this.multilineCommented = false;
    }

}
