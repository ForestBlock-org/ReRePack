package org.crayne.rerepack.workspace.compile.optifine.resource;

import org.apache.commons.lang3.StringUtils;
import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.workspace.predicate.TokenPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CITResource extends OptifineResource {

    @NotNull
    private final String texturePath, textureName;

    public CITResource(@NotNull final Set<TokenPredicate> matches, @NotNull final Token texturePath) {
        super();
        matches.forEach(p -> valueMap().put(p.key().token(), p.value().token()));

        final String texturePathString = texturePath.token();
        final String onlyFilename = texturePathString.contains("/")
                ? StringUtils.substringAfterLast(texturePathString, "/")
                : texturePathString;

        valueMap().put("texture", onlyFilename);
        this.texturePath = texturePathString;
        this.textureName = onlyFilename;
    }

    @NotNull
    public String textureName() {
        return textureName;
    }

    @NotNull
    public String citFilePath() {
        return "assets/minecraft/optifine/cit/" + textureName;
    }

    @NotNull
    public String texturePath() {
        return texturePath;
    }

}
