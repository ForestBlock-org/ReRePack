package org.crayne.rerepack.workspace.compile.optifine.resource.font.space;

import org.crayne.rerepack.workspace.compile.optifine.resource.font.FontResource;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpaceFontResource extends FontResource {

    @NotNull
    public static final String SPACE_FONT_ASSETS_PATH = "assets/space";

    @NotNull
    public static final String SPACE_DEFAULT_FONT_JSON = SPACE_FONT_ASSETS_PATH + "/font/default.json";

    @NotNull
    public static final String SPACE_FONT_SPLITTER = SPACE_FONT_ASSETS_PATH + "/textures/font/splitter.png";

    public void addSpaceFontCharacters() {
        addMagicNumbers();
        addTranslatables();
        super.addSpaceFontCharacters();
    }

    public void addTranslatables() {
        final Map<String, Object> newlayerValues = new LinkedHashMap<>();
        newlayerValues.put("type", "bitmap");
        newlayerValues.put("file", "space:font/splitter.png");
        newlayerValues.put("ascent", -9999);
        newlayerValues.put("height", -2);
        newlayerValues.put("chars", new String[] {"\uDAC0\uDC00"});

        final Map<String, Object> infinitiesValues = new LinkedHashMap<>();
        infinitiesValues.put("type", "space");

        final Map<String, Double> infinities = new LinkedHashMap<>();
        infinities.put("\uDAC0\uDC01", -Double.MIN_VALUE);
        infinities.put("\uDB3F\uDFFF", Double.MAX_VALUE);

        infinitiesValues.put("advances", infinities);

        addCharacterElement(newlayerValues);
        addCharacterElement(infinitiesValues);
    }

    // refer to https://github.com/AmberWat/NegativeSpaceFont#magic-digits-for-data-packs
    public void addMagicNumbers() {
        final Map<String, Object> magicNumbers = new LinkedHashMap<>();
        magicNumbers.put("type", "space");

        final Map<String, Integer> magicNumbersAdvances = new LinkedHashMap<>();
        magicNumbersAdvances.put("-", -6765);
        magicNumbersAdvances.put("9", 1);
        magicNumbersAdvances.put("8", 3);
        magicNumbersAdvances.put("7", 8);
        magicNumbersAdvances.put("6", 21);
        magicNumbersAdvances.put("5", 55);
        magicNumbersAdvances.put("4", 144);
        magicNumbersAdvances.put("3", 377);
        magicNumbersAdvances.put("2", 988);
        magicNumbersAdvances.put("1", 2584);
        magicNumbersAdvances.put("0", 0);

        magicNumbers.put("advances", magicNumbersAdvances);
        addCharacterElement(magicNumbers);
    }

}
