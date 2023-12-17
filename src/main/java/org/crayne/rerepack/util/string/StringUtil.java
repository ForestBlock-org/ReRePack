package org.crayne.rerepack.util.string;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class StringUtil {

    private StringUtil() {}

    public static boolean matchPattern(@NotNull final String pattern, @NotNull final String str) {
        if (pattern.length() == 0 && str.length() == 0) return true;
        if (pattern.length() > 1 && pattern.charAt(0) == '*' && str.length() == 0) return false;

        if ((pattern.length() > 1 && pattern.charAt(0) == '?')
                || (pattern.length() != 0 && str.length() != 0 && pattern.charAt(0) == str.charAt(0)))
            return matchPattern(pattern.substring(1), str.substring(1));

        if (pattern.length() > 0 && pattern.charAt(0) == '*')
            return matchPattern(pattern.substring(1), str) || matchPattern(pattern, str.substring(1));
        return false;
    }

    @NotNull
    public static Set<String> allMatching(@NotNull final Set<String> possibleOptions, @NotNull final String pattern) {
        if (!pattern.contains("*")) {
            final Optional<String> singleMatch = possibleOptions.contains(pattern)
                    ? Optional.of(pattern)
                    : Optional.empty();

            return singleMatch
                    .map(Collections::singleton)
                    .orElse(Collections.emptySet());
        }
        return possibleOptions.stream()
                .filter(i -> StringUtil.matchPattern(pattern.toLowerCase(), i))
                .collect(Collectors.toSet());
    }

    @NotNull
    public static <T> String stringOf(@NotNull final Collection<T> collection) {
        return "{" + (collection.isEmpty() ? "" : "\n") + collection.stream().map(Object::toString).collect(Collectors.joining(", \n")).indent(4) + "}";
    }

}
