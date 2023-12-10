package org.crayne.rerepack.workspace.compile.optifine.resource.cit;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.workspace.compile.optifine.resource.OptifineResource;
import org.crayne.rerepack.workspace.compile.optifine.resource.Resource;
import org.crayne.rerepack.workspace.predicate.TokenPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CITResource extends OptifineResource {

    @NotNull
    public static final String CIT_PATH = "assets/minecraft/optifine/cit/";

    @NotNull
    private final String texturePath, textureName;

    public CITResource(@NotNull final Set<TokenPredicate> matches, @NotNull final Token texturePath) {
        super();
        matches.forEach(p -> valueMap().put(p.key().token(), p.value().token()));

        final String texturePathString = texturePath.token();
        final String onlyFilename = Resource.fileNameOfPath(texturePathString);

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
        return CIT_PATH + textureName;
    }

    @NotNull
    public String texturePath() {
        return texturePath;
    }

}
