package org.crayne.rerepack.workspace;

import org.crayne.rerepack.util.logging.Logger;
import org.crayne.rerepack.workspace.pack.PackFile;
import org.crayne.rerepack.workspace.pack.definition.GlobalDefinitionContainer;
import org.crayne.rerepack.workspace.pack.template.TemplateContainer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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

    @NotNull
    private final Logger logger;

    @NotNull
    private final File directory;

    public Workspace(@NotNull final Logger logger, @NotNull final File directory) {
        this.globalDefinitionContainer = new GlobalDefinitionContainer();
        this.templateContainer = new TemplateContainer();
        this.packFiles = new HashSet<>();
        this.logger = logger;
        this.directory = directory;
    }

    @NotNull
    public File directory() {
        return directory;
    }

    @NotNull
    public Logger logger() {
        return logger;
    }

    @NotNull
    public PackFile createPackage(@NotNull final String name, @NotNull final File file) {
        final PackFile packFile = new PackFile(name, file, this, globalDefinitionContainer);
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

    @NotNull
    public String toString() {
        return "Workspace{" +
                "globalDefinitionContainer=" + globalDefinitionContainer +
                ", templateContainer=" + templateContainer +
                ", packFiles=" + packFiles +
                '}';
    }

}
