package org.crayne.rerepack.workspace.compile.optifine.resource.lang;

import org.crayne.rerepack.workspace.compile.optifine.resource.font.FontResource;
import org.crayne.rerepack.workspace.compile.optifine.resource.font.SpaceFontResource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpaceFontLangResource extends LangResource {

    @NotNull
    public static final String LANG_JSON_FILE = SpaceFontResource.SPACE_FONT_ASSETS_PATH + "/lang/en_us.json";

    public SpaceFontLangResource() {
        super(LANG_JSON_FILE);
    }

    public void addAllSpaces() {
        addTranslatables();
        addAllWholeWidthOffsetsSpaces();
        addAllFractionalOffsetsSpaces();
    }

    public void addTranslatables() {
        values().put("newlayer", "\uDAC0\uDC00");
        values().put("-infinity", "\uDAC0\uDC01");
        values().put("infinity", "\uDB3F\uDFFF");
        values().put("-max", FontResource.createFullWidthSpace(-8192));
        values().put("max", FontResource.createFullWidthSpace(8192));
    }

    @NotNull
    private static final List<Integer> ALLOWED_FRACTIONAL_DENOMINATORS = List.of(
            2, 3, 4, 5, 6, 8, 10, 12, 15, 16, 20, 24, 25, 30, 32, 40, 48, 50, 60, 64, 75, 80, 96, 100
    );

    public void addAllFractionalOffsetsSpaces() {
        for (final int denom : ALLOWED_FRACTIONAL_DENOMINATORS) {
            for (int n = -denom; n <= denom; n++) {
                final double width = (double) n / denom;
                final String space = FontResource.createFractionalSpace(width);
                final String spaceNegative = FontResource.createFractionalSpace(width * -1);
                values().put("space." + n + "/" + denom, space);
                values().put("offset." + n + "/" + denom, space + "%s" + spaceNegative);
            }
        }
    }

    public void addAllWholeWidthOffsetsSpaces() {
        for (int i = -8192; i <= 8192; i++) {
            final String space = FontResource.createFullWidthSpace(i);
            final String spaceNegative = FontResource.createFullWidthSpace(i * -1);
            values().put("space." + i, space);
            values().put("offset." + i, space + "%s" + spaceNegative);
        }
    }

}
