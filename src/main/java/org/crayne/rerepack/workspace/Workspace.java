package org.crayne.rerepack.workspace;

import org.crayne.rerepack.workspace.pack.PackFile;
import org.crayne.rerepack.workspace.pack.definition.GlobalDefinitionContainer;
import org.crayne.rerepack.workspace.pack.template.TemplateContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Workspace {

    @NotNull
    private final GlobalDefinitionContainer globalDefinitionContainer;

    @NotNull
    private final TemplateContainer templateContainer;

    @NotNull
    private final Set<PackFile> packFiles;

    public Workspace() {
        this.globalDefinitionContainer = new GlobalDefinitionContainer();
        this.templateContainer = new TemplateContainer();
        this.packFiles = new HashSet<>();
    }

    @NotNull
    public PackFile createPackage(@NotNull final String name) {
        final PackFile packFile = new PackFile(name, this, globalDefinitionContainer);
        packFiles.add(packFile);
        return packFile;
    }

    @NotNull
    public Set<PackFile> packFiles() {
        return Collections.unmodifiableSet(packFiles);
    }

    @NotNull
    public GlobalDefinitionContainer globalDefinitionContainer() {
        return globalDefinitionContainer;
    }

    @NotNull
    public TemplateContainer templateContainer() {
        return templateContainer;
    }

}
