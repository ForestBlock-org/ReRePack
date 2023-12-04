package org.crayne.rerepack.util.logging.message;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.util.logging.Logger;
import org.crayne.rerepack.util.logging.LoggingLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PositionInformationMessage extends AbstractLoggingMessage {

    @NotNull
    private final Token at;

    @NotNull
    private final String lineInCode;

    private final boolean skipToEnd;

    public PositionInformationMessage(@NotNull final LoggingLevel level,
                            @NotNull final Token at, @NotNull final String lineInCode,
                            final boolean skipToEnd) {
        super("at line " + at.line() + ", " + "column " + at.column()
                + (at.file() != null ? " in file " + at.file().getAbsolutePath() : ""), level);

        this.at = at;
        this.lineInCode = lineInCode;
        this.skipToEnd = skipToEnd;
    }

    public static class Builder {

        @Nullable
        private Token at;

        @Nullable
        private String lineInCode;

        private boolean skipToEnd;

        @NotNull
        private LoggingLevel level;

        public Builder(@NotNull final LoggingLevel level) {
            this.level = level;
        }

        @NotNull
        public static Builder createBuilder(@NotNull final LoggingLevel level) {
            return new Builder(level);
        }

        @Nullable
        public LoggingLevel level() {
            return level;
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
        public Builder at(@NotNull final Token at) {
            this.at = at;
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
        public PositionInformationMessage build() {
            if (at == null)
                throw new UnsupportedOperationException("Cannot create traceback message if at-token is null");

            if (lineInCode == null)
                throw new UnsupportedOperationException("Cannot create traceback message if line in code is null");

            return new PositionInformationMessage(level, at, lineInCode, skipToEnd);
        }

    }

    public void printTo(@NotNull final Logger logger) {
        logger.log(message(), level());

        final int cursorOffset = skipToEnd ? lineInCode.length() : at.column() - 1;
        final String cursorOffsetSpace = " ".repeat(Math.max(0, cursorOffset));

        logger.log(lineInCode.replace("\t", " "), LoggingLevel.INFO);
        logger.log(cursorOffsetSpace + "^", LoggingLevel.INFO);
    }



}
