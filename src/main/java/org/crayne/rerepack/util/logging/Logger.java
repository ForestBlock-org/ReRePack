package org.crayne.rerepack.util.logging;

import org.crayne.rerepack.util.color.ansi.TextColor;
import org.crayne.rerepack.util.logging.message.AbstractLoggingMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.time.LocalDateTime;

public class Logger {

    @NotNull
    private final PrintStream out;

    @NotNull
    private final String format;

    public Logger() {
        out = System.out;
        format = "[%hour%:%min%:%sec%] (%c%%ll%%cr%): %c%%m%%cr%";
    }

    public Logger(@NotNull final String format) {
        out = System.out;
        this.format = format;
    }

    public Logger(@NotNull final String format, @NotNull final PrintStream out) {
        this.out = out;
        this.format = format;
    }

    public void println(@Nullable final Object x) {
        if (x == null) return;
        info(String.valueOf(x));
    }

    public void log(@NotNull final String message, @NotNull final LoggingLevel level) {
        final String levelString = level.name().replace("_", " ");
        final String levelStringLowercase = levelString.toLowerCase();
        final String colorString = level.color().toString();
        final LocalDateTime now = LocalDateTime.now();

        out.println(format
                .replace("%min%",  "%02d".formatted(now.getMinute()))
                .replace("%hour%", "%02d".formatted(now.getHour()))
                .replace("%sec%",  "%02d".formatted(now.getSecond()))
                .replace("%l%", levelString)
                .replace("%ll%", levelStringLowercase)
                .replace("%c%", colorString)
                .replace("%cr%", TextColor.RESET.toString())
                .replace("%m%", message));
    }

    public void info(@NotNull final String message) {
        log(message, LoggingLevel.INFO);
    }

    public void warn(@NotNull final String message) {
        log(message, LoggingLevel.WARN);
    }

    public void error(@NotNull final String message) {
        log(message, LoggingLevel.ERROR);
    }

    public void log(@NotNull final AbstractLoggingMessage message) {
        message.printTo(this);
    }

}
