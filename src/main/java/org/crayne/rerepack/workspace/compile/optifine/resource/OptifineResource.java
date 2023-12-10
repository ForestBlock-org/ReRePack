package org.crayne.rerepack.workspace.compile.optifine.resource;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class OptifineResource implements Resource {

    @NotNull
    private final Map<String, String> valueMap;

    public OptifineResource() {
        this.valueMap = new LinkedHashMap<>();
    }

    @NotNull
    public Map<String, String> valueMap() {
        return valueMap;
    }

    @NotNull
    public String encode() {
        return valueMap.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n"));
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final OptifineResource that = (OptifineResource) o;

        return valueMap.equals(that.valueMap);
    }

    public int hashCode() {
        return valueMap.hashCode();
    }
}
