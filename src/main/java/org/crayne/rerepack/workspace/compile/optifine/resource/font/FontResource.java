package org.crayne.rerepack.workspace.compile.optifine.resource.font;

import org.crayne.rerepack.workspace.compile.optifine.resource.JsonResource;
import org.crayne.rerepack.workspace.compile.optifine.resource.Resource;
import org.crayne.rerepack.workspace.pack.character.CharacterStatement;
import org.crayne.rerepack.workspace.pack.definition.Definition;
import org.crayne.rerepack.workspace.predicate.TokenPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FontResource implements JsonResource {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @NotNull
    private final List<Map<String, Object>> providers;

    @NotNull
    public static final String DEFAULT_FONT_JSON = "assets/minecraft/font/default.json";

    public FontResource() {
        this.providers = new ArrayList<>();
    }

    public void addSpaceFontCharacters() {
        addFullSpaceCharacters();
        addFractionalSpaceCharacters();
    }

    public void addFractionalSpaceCharacters() {
        final Map<String, Object> values = new LinkedHashMap<>();
        values.put("type", "space");

        final Map<String, Double> spaces = new LinkedHashMap<>();
        values.put("advances", spaces);

        for (int n = -4800; n <= 4800; n++) {
            final double width = (double) n / 4800.0d;
            spaces.put(createFractionalSpace(width), width);
        }

        addCharacterElement(values);
    }

    public void addFullSpaceCharacters() {
        final Map<String, Object> values = new LinkedHashMap<>();
        values.put("type", "space");

        final Map<String, Integer> spaces = new LinkedHashMap<>();
        values.put("advances", spaces);

        for (int i = -8192; i <= 8192; i++)
            spaces.put(createFullWidthSpace(i), i);

        addCharacterElement(values);
    }

    @NotNull
    public static String createFullWidthSpace(final int width) {
        if (width < -8192 || width > 8192) throw new IllegalArgumentException("Width must be -8192 <= width <= 8192");

        final int code = 0xD0000 + width;
        return Character.toString(code);
    }

    @NotNull
    public static String createFractionalSpace(final double width) {
        if (width < -1 || width > 1) throw new IllegalArgumentException("Width must be -1 <= width <= 1");

        final int code = 0x50000 + (int) Math.round(width * 4800.0d);
        return Character.toString(code);
    }

    public void addCharacterElement(@NotNull final CharacterStatement characterStatement) {
        final Map<String, Object> values = new LinkedHashMap<>();
        values.put("type", "bitmap");
        values.put("file", "minecraft:" + Resource.fileNameOfPath(characterStatement.bitmapFilePath().token()));

        characterStatement.definitionContainer()
                .definitions()
                .values()
                .stream()
                .map(Definition::fullDefinition)
                .forEach(t -> putJsonDefinition(values, t));

        values.put("chars", characterStatement.characterList());
        addCharacterElement(values);
    }

    public static void putJsonDefinition(@NotNull final Map<String, Object> values, @NotNull final TokenPredicate t) {
        final String key = t.key().token(), value = t.value().token();

        try {
            final int valueInt = Integer.parseInt(value);
            values.put(key, valueInt);
        } catch (final NumberFormatException e) {
            try {
                final double valueDouble = Double.parseDouble(value);
                values.put(key, valueDouble);
            } catch (final NumberFormatException e2) {
                values.put(key, value);
            }
        }
    }

    public void addCharacterElement(@NotNull final Map<String, Object> values) {
        providers.add(values);
    }

}
