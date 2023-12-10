package org.crayne.rerepack.util.logging.message;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.util.logging.Logger;
import org.crayne.rerepack.util.logging.LoggingLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class TraceBackMessage extends AbstractLoggingMessage {

    @NotNull
    private final String @NotNull [] hints;

    @NotNull
    private final PositionInformationMessage positionInformationMessage;

    public TraceBackMessage(@NotNull final String message, @NotNull final LoggingLevel level,
                            @NotNull final Token at, @Nullable final String lineInCode,
                            final boolean skipToEnd,
                            @NotNull final String @NotNull ... hints) {
        super(message, level);
        this.hints = hints;
        this.positionInformationMessage = new PositionInformationMessage(level, at, lineInCode, skipToEnd);
    }

    public void printTo(@NotNull final Logger logger) {
        logger.log(message(), level());
        Arrays.stream(hints).forEach(h -> logger.log(h, LoggingLevel.HELP));

        logger.log(positionInformationMessage);
    }

    public static class Builder {

        @Nullable
        private Token at;

        @Nullable
        private String lineInCode;

        @NotNull
        private String @NotNull [] hints;

        private boolean skipToEnd;

        @NotNull
        private String message;

        @NotNull
        private LoggingLevel level;

        public Builder(@NotNull final String message, @NotNull final LoggingLevel level) {
            this.message = message;
            this.level = level;
            this.hints = new String[0];
        }

        @NotNull
        public static Builder createBuilder(@NotNull final String message,
                                            @NotNull final LoggingLevel level) {
            return new Builder(message, level);
        }

        @Nullable
        public LoggingLevel level() {
            return level;
        }

        @NotNull
        public String message() {
            return message;
        }

        @Nullable
        public Token at() {
            return at;
        }

        public boolean skipToEnd() {
            return skipToEnd;
        }

        @Nullable
        public String lineInCode() {
            return lineInCode;
        }

        @NotNull
        public String @Nullable [] hints() {
            return hints;
        }

        @NotNull
        public Builder at(@NotNull final Token at) {
            this.at = at;
            return this;
        }

        @NotNull
        public Builder hints(@NotNull final String @NotNull ... hints) {
            this.hints = hints;
            return this;
        }

        @NotNull
        public Builder message(@NotNull final String message) {
            this.message = message;
            return this;
        }

        @NotNull
        public Builder level(@NotNull final LoggingLevel level) {
            this.level = level;
            return this;
        }

        @NotNull
        public Builder skipToEnd(final boolean skipToEnd) {
            this.skipToEnd = skipToEnd;
            return this;
        }

        @NotNull
        public Builder lineInCode(@NotNull final String lineInCode) {
            this.lineInCode = lineInCode;
            return this;
        }

        @NotNull
        public Builder positionInformation(@NotNull final Token at, @NotNull final List<String> codeLines) {
            this.at = at;
            return lineInCode(codeLines);
        }

        @NotNull
        public Builder lineInCode(@NotNull final List<String> codeLines) {
            if (at == null)
                throw new UnsupportedOperationException("Cannot find line in code without 'at' informatio");

            final int line = at.line() - 1;
            if (line < 0 || line >= codeLines.size())
                throw new IllegalArgumentException("Cannot create traceback for invalid line in code");

            return lineInCode(codeLines.get(line));
        }

        @NotNull
        public TraceBackMessage build() {
            if (at == null)
                throw new UnsupportedOperationException("Cannot create traceback message if at-token is null");

            return new TraceBackMessage(message, level, at, lineInCode, skipToEnd, hints);
        }

    }

}
