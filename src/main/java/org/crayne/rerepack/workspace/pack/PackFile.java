package org.crayne.rerepack.workspace.pack;

import org.crayne.rerepack.workspace.Workspace;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.pack.definition.GlobalDefinitionContainer;
import org.crayne.rerepack.workspace.pack.match.MatchReplaceContainer;
import org.crayne.rerepack.workspace.pack.template.use.UseContainer;
import org.crayne.rerepack.workspace.pack.write.WriteContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.jetbrains.annotations.NotNull;

public class PackFile implements PackScope, Initializable {

    @NotNull
    private final String namespacedKey;

    @NotNull
    private final DefinitionContainer definitionContainer;

    @NotNull
    private final MatchReplaceContainer matchReplaceContainer;

    @NotNull
    private final WriteContainer writeContainer;

    @NotNull
    private final UseContainer useContainer;

    @NotNull
    private final Workspace workspace;

    public PackFile(@NotNull final String namespacedKey, @NotNull final Workspace workspace,
                    @NotNull final GlobalDefinitionContainer parent) {
        this.namespacedKey = namespacedKey;
        this.workspace = workspace;
        this.definitionContainer = new DefinitionContainer(parent);
        this.matchReplaceContainer = new MatchReplaceContainer(definitionContainer);
        this.writeContainer = new WriteContainer(definitionContainer);
        this.useContainer = new UseContainer(definitionContainer);
    }

    public void initialize() throws WorkspaceException {
        definitionContainer().initialize();
        matchReplaceContainer().initialize();
        writeContainer().initialize();
        useContainer().initialize();
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
    public UseContainer useContainer() {
        return useContainer;
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
                ", definitionContainer=" + definitionContainer +
                ", matchReplaceContainer=" + matchReplaceContainer +
                ", writeContainer=" + writeContainer +
                ", useContainer=" + useContainer +
                //", workspace=" + workspace +
                '}';
    }

}
