package org.crayne.rerepack.workspace.compile.optifine.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

public interface JsonResource extends Resource {

    @NotNull
    Gson GSON = new GsonBuilder().create();

    @NotNull
    default String encode() {
        return GSON.toJson(this);
    }

}
