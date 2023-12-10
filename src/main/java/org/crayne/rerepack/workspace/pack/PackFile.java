package org.crayne.rerepack.workspace.pack;

import org.crayne.rerepack.workspace.Workspace;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.character.CharacterContainer;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.pack.definition.GlobalDefinitionContainer;
import org.crayne.rerepack.workspace.pack.match.MatchReplaceContainer;
import org.crayne.rerepack.workspace.pack.template.use.UseContainer;
import org.crayne.rerepack.workspace.pack.write.WriteContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.jetbrains.annotations.NotNull;

import java.io.File;

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
    private final CharacterContainer characterContainer;

    @NotNull
    private final Workspace workspace;

    @NotNull
    private final File file;

    public PackFile(@NotNull final String namespacedKey, @NotNull final File file,
                    @NotNull final Workspace workspace, @NotNull final GlobalDefinitionContainer parent) {
        this.file = file;
        this.namespacedKey = namespacedKey;
        this.workspace = workspace;
        this.definitionContainer = new DefinitionContainer(parent);
        this.matchReplaceContainer = new MatchReplaceContainer(definitionContainer);
        this.writeContainer = new WriteContainer(definitionContainer);
        this.useContainer = new UseContainer(definitionContainer);
        this.characterContainer = new CharacterContainer(definitionContainer);
    }

    @NotNull
    public File file() {
        return file;
    }

    public void initialize() throws WorkspaceException {
        definitionContainer().initialize();
        matchReplaceContainer().initialize();
        writeContainer().initialize();
        useContainer().initialize();
        characterContainer().initialize();
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
    public CharacterContainer characterContainer() {
        return characterContainer;
    }

    @NotNull
    public String toString() {
        return "PackFile{" +
                "namespacedKey='" + namespacedKey + '\'' +
                ", definitionContainer=" + definitionContainer +
                ", matchReplaceContainer=" + matchReplaceContainer +
                ", writeContainer=" + writeContainer +
                ", useContainer=" + useContainer +
                '}';
    }

}
