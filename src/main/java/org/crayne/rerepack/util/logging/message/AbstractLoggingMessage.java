package org.crayne.rerepack.util.logging.message;

import org.crayne.rerepack.util.logging.Logger;
import org.crayne.rerepack.util.logging.LoggingLevel;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractLoggingMessage {

    @NotNull
    private final String message;

    @NotNull
    private final LoggingLevel level;

    public AbstractLoggingMessage(@NotNull final String message, @NotNull final LoggingLevel level) {
        this.message = message;
        this.level = level;
    }

    @NotNull
    public String message() {
        return message;
    }

    @NotNull
    public LoggingLevel level() {
        return level;
    }

    public abstract void printTo(@NotNull final Logger logger);

    @NotNull
    public String toString() {
        return "AbstractLoggingMessage{" +
                "message='" + message + '\'' +
                ", level=" + level +
                '}';
    }

}
