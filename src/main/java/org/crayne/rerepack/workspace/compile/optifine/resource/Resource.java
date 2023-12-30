package org.crayne.rerepack.workspace.compile.optifine.resource;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public interface Resource {

    @NotNull
    String encode();

    @NotNull
    static String fileNameOfPath(@NotNull final String filepath) {
        return filepath.replace("/", "_");
    }

    @NotNull
    static String withDifferentFileExtension(@NotNull final String filepath, @NotNull final String extension) {
        return StringUtils.substringBefore(filepath, ".") + extension;
    }

}
