package org.crayne.rerepack.util.logging.message;

import org.crayne.rerepack.util.logging.Logger;
import org.crayne.rerepack.util.logging.LoggingLevel;
import org.jetbrains.annotations.NotNull;

public class LoggingMessage extends AbstractLoggingMessage {

    public LoggingMessage(@NotNull final String message, @NotNull final LoggingLevel level) {
        super(message, level);
    }

    public void printTo(@NotNull final Logger logger) {
        logger.log(message(), level());
    }

    @NotNull
    public static LoggingMessage info(@NotNull final String message) {
        return new LoggingMessage(message, LoggingLevel.INFO);
    }

    @NotNull
    public static LoggingMessage error(@NotNull final String message) {
        return new LoggingMessage(message, LoggingLevel.ERROR);
    }

    @NotNull
    public static LoggingMessage warn(@NotNull final String message) {
        return new LoggingMessage(message, LoggingLevel.WARN);
    }

    @NotNull
    public static LoggingMessage help(@NotNull final String message) {
        return new LoggingMessage(message, LoggingLevel.HELP);
    }

    @NotNull
    public static LoggingMessage success(@NotNull final String message) {
        return new LoggingMessage(message, LoggingLevel.SUCCESS);
    }

    @NotNull
    public static LoggingMessage parsingError(@NotNull final String message) {
        return new LoggingMessage(message, LoggingLevel.PARSING_ERROR);
    }

    @NotNull
    public static LoggingMessage lexingError(@NotNull final String message) {
        return new LoggingMessage(message, LoggingLevel.LEXING_ERROR);
    }

    @NotNull
    public static LoggingMessage convertingError(@NotNull final String message) {
        return new LoggingMessage(message, LoggingLevel.CONVERTING_ERROR);
    }

    @NotNull
    public static LoggingMessage definitionError(@NotNull final String message) {
        return new LoggingMessage(message, LoggingLevel.DEFINITION_ERROR);
    }

    @NotNull
    public static LoggingMessage packingError(@NotNull final String message) {
        return new LoggingMessage(message, LoggingLevel.PACKING_ERROR);
    }

    @NotNull
    public static LoggingMessage analyzingError(@NotNull final String message) {
        return new LoggingMessage(message, LoggingLevel.ANALYZING_ERROR);
    }

}
