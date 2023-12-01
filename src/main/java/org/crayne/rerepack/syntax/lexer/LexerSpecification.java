package org.crayne.rerepack.syntax.lexer;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class LexerSpecification {

    @NotNull
    private final Set<String> extendedSpecialCharacters;

    @NotNull
    private final Set<Character> singleSpecialCharacters;

    public LexerSpecification(@NotNull final Collection<Character> singleSpecialCharacters) {
        this.singleSpecialCharacters = Set.copyOf(singleSpecialCharacters);
        this.extendedSpecialCharacters = Collections.emptySet();
    }

    public LexerSpecification(@NotNull final Collection<Character> singleSpecialCharacters,
                              @NotNull final Collection<String> extendedSpecialCharacters) {

        this.singleSpecialCharacters = Set.copyOf(singleSpecialCharacters);
        validateExtendedSpecialCharacters(extendedSpecialCharacters);
        this.extendedSpecialCharacters = Set.copyOf(extendedSpecialCharacters);
    }

    @NotNull
    public abstract Set<Character> validStringLiteralCharacters();

    public boolean validStringLiteralChar(final char character) {
        return validStringLiteralCharacters().contains(character);
    }

    public boolean validStringLiteralChar(@NotNull final String character) {
        return character.length() == 1 && validStringLiteralChar(character.charAt(0));
    }

    @NotNull
    public abstract String singleLineCommentBegin();

    @NotNull
    public abstract String multiLineCommentBegin();

    @NotNull
    public abstract String multiLineCommentEnd();

    @NotNull
    public abstract Optional<String> parseStringLiteral(@NotNull final String string);

    public abstract boolean isStringLiteral(@NotNull final String string);

    public abstract boolean isKeywordLiteral(@NotNull final String string);

    @NotNull
    public abstract Optional<Integer> parseIntegerLiteral(@NotNull final String string);

    public boolean isIntegerLiteral(@NotNull final String string) {
        return parseIntegerLiteral(string).isPresent();
    }

    @NotNull
    public abstract String removeStringLiterals(@NotNull final String string);

    @NotNull
    public abstract String addStringLiterals(@NotNull final String string);

    public abstract boolean validEscapeSequence(@NotNull final String string);

    @NotNull
    public abstract String stringEscapeRegex();

    public boolean charactersEscaped(@NotNull final String seq) {
        for (final String escapeSequence : seq.split(stringEscapeRegex())) {
            if (!validEscapeSequence(escapeSequence) && escapeSequence.startsWith("\\")) return false;
        }
        return true;
    }

    @NotNull
    public Set<String> extendedSpecialCharacters() {
        return extendedSpecialCharacters;
    }

    public static boolean hasQuotes(@NotNull final String string, final char quoteCharacter) {
        return string.startsWith(quoteCharacter + "") && string.endsWith(quoteCharacter + "");
    }

    public void validateExtendedSpecialCharacters(@NotNull final Collection<String> extendedSpecialCharacters) {
        for (final String specialCharacter : extendedSpecialCharacters) {
            if (isExtendedSpecialCharacter(specialCharacter)) continue;

            throw new IllegalArgumentException("Not an extended special token: '"
                    + specialCharacter + "'. Not all characters are contained in special character set.");
        }
    }

    public boolean isAnySpecialToken(@NotNull final String token) {
        return isSpecialToken(token) || isExtendedSpecialCharacter(token);
    }

    public boolean isSpecialToken(@NotNull final String token) {
        return token.length() == 1 && isSpecialCharacter(token.charAt(0));
    }

    public boolean isSpecialCharacter(final char character) {
        return singleSpecialCharacters.contains(character);
    }

    public boolean isExtendedSpecialCharacter(@NotNull final String token) {
        return Arrays.stream(token.split("")).allMatch(this::isSpecialToken);
    }

}
