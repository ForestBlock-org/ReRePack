package org.crayne.rerepack.workspace.compile.optifine.resource.lang;

import com.google.gson.Gson;
import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.util.minecraft.LanguageFiles;
import org.crayne.rerepack.workspace.compile.optifine.resource.JsonResource;
import org.crayne.rerepack.workspace.pack.lang.LangStatement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LangResource implements JsonResource {

    @NotNull
    private final Map<String, String> values;

    @NotNull
    private final transient String languageFile;

    public LangResource(@NotNull final String languageFile) {
        this.languageFile = languageFile;
        this.values = new LinkedHashMap<>();
    }

    @NotNull
    public static LangResource ofDefault(@NotNull final String languageName) {
        return new LangResource("assets/minecraft/lang/" + languageName + ".json");
    }

    @NotNull
    public static Set<LangResource> createLangResources(@NotNull final LangStatement langStatement) {
        return langStatement.languageFileMatches().stream()
                .map(Token::token)
                .map(LanguageFiles::allMatching)
                .flatMap(Collection::stream)
                .map(LangResource::ofDefault)
                .collect(Collectors.toSet());
    }

    public void addLangStatementElements(@NotNull final LangStatement langStatement) {
        langStatement.replacements().forEach(predicate -> values
                .put(predicate.key().token(), predicate.value().token()));
    }

    @NotNull
    public String languageName() {
        return languageFile;
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final LangResource that = (LangResource) o;

        return languageFile.equals(that.languageFile);
    }

    public int hashCode() {
        return languageFile.hashCode();
    }

    @NotNull
    public String toString() {
        return "LangResource{" +
                "values=" + values +
                ", languageFile='" + languageFile + '\'' +
                '}';
    }

    @NotNull
    public Map<String, String> values() {
        return values;
    }

    @NotNull
    public String encode() {
        return new Gson().toJson(values);
    }

}
