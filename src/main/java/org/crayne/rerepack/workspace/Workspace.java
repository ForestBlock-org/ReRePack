package org.crayne.rerepack.workspace;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.workspace.util.pack.Pack;
import org.crayne.rerepack.workspace.util.pack.PackException;
import org.crayne.rerepack.workspace.util.replacement.template.Template;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Workspace extends AbstractWorkspace {

    @NotNull
    private final Map<String, Template> templates;

    @NotNull
    private final Map<String, Pack> packages;

    public Workspace() {
        super();
        templates = new HashMap<>();
        packages = new HashMap<>();
    }

    @NotNull
    public Map<String, Template> templates() {
        return templates;
    }

    public void createTemplate(@NotNull final Token name, @NotNull final Template template) {
        if (templates().containsKey(name.token())) throw new PackException(name, "A template named '" + name.token() + "' already exists; " +
                "Cannot create a new one with the same name");

        templates().put(name.token(), template);
    }

    @NotNull
    public Optional<Template> findTemplate(@NotNull final String name) {
        return Optional.ofNullable(templates().get(name));
    }

    @NotNull
    public Template template(@NotNull final Token name) {
        return findTemplate(name.token())
                .orElseThrow(() -> new PackException(name, "Cannot find a template named '"
                        + name + "' in this workspace"));
    }

    @NotNull
    public Map<String, Pack> packages() {
        return packages;
    }

    @NotNull
    public Pack createPackage(@NotNull final String name) {
        if (packages().containsKey(name)) throw new PackException("A pack named '" + name + "' already exists; " +
                "Cannot create a new one with the same name");

        final Pack pack = new Pack(name, this);
        packages().put(name, pack);
        return pack;
    }

}
