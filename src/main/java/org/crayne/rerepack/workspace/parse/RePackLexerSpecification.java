package org.crayne.rerepack.workspace.parse;

import org.crayne.rerepack.syntax.lexer.LexerSpecification;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RePackLexerSpecification extends LexerSpecification {

    @NotNull
    private static final String RAW_STRING_ESCAPE_REGEX = "\\\\u[\\dA-Fa-f]{4}|\\\\\\d|\\\\[\"\\\\'tnbfr]";

    @NotNull
    private static final String STRING_ESCAPE_REGEX = splitKeepDelim(RAW_STRING_ESCAPE_REGEX);

    @NotNull
    private static final Set<Character> VALID_STRING_LITERAL_CHARACTERS = Set.of('\'', '"');

    @NotNull
    private static final Set<String> KEYWORDS = Set.of(
            "def",
            "global",

            "template",
            "require",

            "match",
            "replace",
            "items",

            "write",
            "copy",

            "char",

            "lang"
    );

    @NotNull
    public static final RePackLexerSpecification INSTANCE = new RePackLexerSpecification();

    public RePackLexerSpecification() {
        super(List.of(
                ',', '(', ')', '{', '}', '/', '*', '=', '\'', '"', '>'
        ), List.of(
                "=>"
        ));
    }

    @NotNull
    public Set<Character> validStringLiteralCharacters() {
        return VALID_STRING_LITERAL_CHARACTERS;
    }

    @NotNull
    public String singleLineCommentBegin() {
        return "//";
    }

    @NotNull
    public String multiLineCommentBegin() {
        return "/*";
    }

    @NotNull
    public String multiLineCommentEnd() {
        return "*/";
    }

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private static String splitKeepDelim(@NotNull final String delimRegex) {
        return "((?=" + delimRegex + ")|(?<=" + delimRegex + "))";
    }

    public static boolean hasQuotes(@NotNull final String string, final char quoteCharacter) {
        return string.startsWith(quoteCharacter + "") && string.endsWith(quoteCharacter + "");
    }

    public boolean isStringLiteral(@NotNull final String string) {
        return hasQuotes(string, '"') || hasQuotes(string, '\'');
    }

    public boolean isKeywordLiteral(@NotNull final String string) {
        return KEYWORDS.contains(string);
    }

    @NotNull
    public Optional<String> parseStringLiteral(@NotNull final String string) {
        return isStringLiteral(string) ? Optional.of(string) : Optional.empty();
    }

    @NotNull
    public Optional<Integer> parseIntegerLiteral(@NotNull final String string) {
        try {
            return Optional.of(Integer.parseInt(string));
        } catch (final NumberFormatException e) {
            return Optional.empty();
        }
    }

    @NotNull
    public String removeStringLiterals(@NotNull final String string) {
        if (!isStringLiteral(string)) return string;
        return string.substring(1, string.length() - 1);
    }

    @NotNull
    public String addStringLiterals(@NotNull final String string) {
        return "\"" + string + "\"";
    }

    public boolean validEscapeSequence(@NotNull final String seq) {
        return seq.matches(RAW_STRING_ESCAPE_REGEX);
    }

    @NotNull
    public String stringEscapeRegex() {
        return STRING_ESCAPE_REGEX;
    }

}
