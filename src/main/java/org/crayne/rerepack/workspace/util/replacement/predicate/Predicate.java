package org.crayne.rerepack.workspace.util.replacement.predicate;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.util.minecraft.VanillaItem;
import org.crayne.rerepack.workspace.util.def.DefinitionContainer;
import org.crayne.rerepack.workspace.util.except.DefinitionException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Predicate {

    @NotNull
    private final Token key, value;

    public Predicate(@NotNull final Token key, @NotNull final Token value) {
        this.key = key;
        this.value = value;
    }

    @NotNull
    public Token value() {
        return value;
    }

    @NotNull
    public Token key() {
        return key;
    }

    @NotNull
    public static Set<Token> parseItems(@NotNull final Token key) {
        final String keyValue = key.token();
        final boolean moddedItem = VanillaItem.moddedItem(keyValue);
        if (moddedItem) return Collections.singleton(key);

        return VanillaItem.allMatching(keyValue).stream().map(VanillaItem::name).map(s -> Token.of(s, key)).collect(Collectors.toSet());
    }

    @NotNull
    public static Token parseDefinitions(@NotNull final Token name,
                                         @NotNull final Token value,
                                         @NotNull final DefinitionContainer @NotNull ... definitionContainers) {
        String result = value.token();

        final Matcher definitionMatcher = Pattern.compile(("\\$\\((.*?)\\)")).matcher(value.token());
        while (definitionMatcher.find()) {
            final Token definitionName = Token.of(definitionMatcher.group(1), name);
            final Optional<DefinitionContainer> foundDefinitionContainer = Arrays.stream(definitionContainers)
                    .filter(d -> d.defined(definitionName))
                    .findAny();

            if (foundDefinitionContainer.isEmpty())
                throw new DefinitionException(value, "Cannot find definition '" + definitionName.token() + "'");

            final Token definitionValue = foundDefinitionContainer.get().definition(definitionName, value);
            final boolean initialized = foundDefinitionContainer.get().definitionInitialized(definitionName);

            if (!initialized)
                throw new DefinitionException(value, "Invalid use of uninitialized variable '" + definitionName.token() + "'");

            result = result.replace("$(" + definitionName.token() + ")", definitionValue.token());
        }
        return Token.of(result, value);
    }

    @NotNull
    public String toString() {
        return "Predicate{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }

}
