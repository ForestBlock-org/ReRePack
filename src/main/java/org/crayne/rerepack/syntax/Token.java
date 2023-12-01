package org.crayne.rerepack.syntax;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public record Token(@NotNull String token, int line, int column, @Nullable File file) {

    @NotNull
    public static Token of(@NotNull final String token, final int line, final int column, @Nullable final File file) {
        return new Token(token, line, column, file);
    }

    @NotNull
    public static Token of(@NotNull final String value, @NotNull final Token token) {
        return new Token(value, token.line, token.column, token.file);
    }

    @NotNull
    public static Token of(@NotNull final String value) {
        return new Token(value, -1, -1, null);
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Token token1 = (Token) o;

        return token.equals(token1.token);
    }

    public int hashCode() {
        return token.hashCode();
    }

    public String toString() {
        return "Token{" +
                "token='" + token + '\'' +
                ", line=" + line +
                ", column=" + column +
                ", file=" + file +
                '}';
    }
}
