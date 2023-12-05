package org.crayne.rerepack.workspace.pack;

import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.pack.match.MatchReplaceContainer;
import org.crayne.rerepack.workspace.pack.template.use.UseContainer;
import org.crayne.rerepack.workspace.pack.write.WriteContainer;
import org.jetbrains.annotations.NotNull;

public interface PackScope {

    @NotNull
    DefinitionContainer definitionContainer();

    @NotNull
    MatchReplaceContainer matchReplaceContainer();

    @NotNull
    WriteContainer writeContainer();

    @NotNull
    UseContainer useContainer();


}
