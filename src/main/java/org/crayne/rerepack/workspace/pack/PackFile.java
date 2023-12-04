package org.crayne.rerepack.workspace.pack;

import org.crayne.rerepack.workspace.Workspace;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.pack.definition.GlobalDefinitionContainer;
import org.crayne.rerepack.workspace.pack.match.MatchReplaceContainer;
import org.crayne.rerepack.workspace.pack.write.WriteContainer;
import org.jetbrains.annotations.NotNull;

public class PackFile implements PackScope {

    @NotNull
    private final String namespacedKey;

    @NotNull
    private final DefinitionContainer definitionContainer;

    @NotNull
    private final MatchReplaceContainer matchReplaceContainer;

    @NotNull
    private final WriteContainer writeContainer;

    @NotNull
    private final Workspace workspace;

    public PackFile(@NotNull final String namespacedKey, @NotNull final Workspace workspace,
                    @NotNull final GlobalDefinitionContainer parent) {
        this.namespacedKey = namespacedKey;
        this.workspace = workspace;
        this.definitionContainer = new DefinitionContainer(parent);
        this.matchReplaceContainer = new MatchReplaceContainer();
        this.writeContainer = new WriteContainer();
    }

    @NotNull
    public String namespacedKey() {
        return namespacedKey;
    }

    @NotNull
    public Workspace workspace() {
        return workspace;
    }

    @NotNull
    public DefinitionContainer definitionContainer() {
        return definitionContainer;
    }

    @NotNull
    public WriteContainer writeContainer() {
        return writeContainer;
    }

    @NotNull
    public MatchReplaceContainer matchReplaceContainer() {
        return matchReplaceContainer;
    }

    @NotNull
    public String toString() {
        return "PackFile{" +
                "namespacedKey='" + namespacedKey + '\'' +
                '}';
    }

}
