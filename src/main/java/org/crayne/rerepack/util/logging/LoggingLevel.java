package org.crayne.rerepack.util.logging;

import org.crayne.rerepack.util.color.Color;
import org.crayne.rerepack.util.color.ansi.TextColor;
import org.jetbrains.annotations.NotNull;

import static org.crayne.rerepack.util.color.ansi.TextColor.foreground;

public enum LoggingLevel {

    INFO             (foreground(Color.rgb(255, 255, 255))),
    SUCCESS          (foreground(Color.rgb(0, 255, 0))),
    HELP             (foreground(Color.rgb(0, 255, 255))),
    WARN             (foreground(Color.rgb(255, 255, 0))),

    ERROR            (foreground(Color.rgb(255, 0, 0))),
    LEXING_ERROR     (foreground(Color.rgb(255, 0, 0))),
    PARSING_ERROR    (foreground(Color.rgb(255, 0, 0))),
    DEFINITION_ERROR (foreground(Color.rgb(255, 0, 0))),
    WORKSPACE_ERROR  (foreground(Color.rgb(255, 0, 0))),
    CONVERTING_ERROR (foreground(Color.rgb(255, 0, 0))),
    PACKING_ERROR    (foreground(Color.rgb(255, 0, 0)));

    @NotNull
    private final TextColor color;

    LoggingLevel(@NotNull final TextColor color) {
        this.color = color;
    }

    @NotNull
    public TextColor color() {
        return color;
    }

    public boolean error() {
        return switch (this) {
            case ERROR, LEXING_ERROR, PARSING_ERROR,
                    CONVERTING_ERROR, PACKING_ERROR, WORKSPACE_ERROR -> true;
            default -> false;
        };
    }

}
