package org.crayne.rerepack;

import org.crayne.rerepack.syntax.parser.ExpressionParser;
import org.crayne.rerepack.util.minecraft.VanillaItems;
import org.crayne.rerepack.workspace.Workspace;
import org.crayne.rerepack.workspace.WorkspaceBuilder;
import org.crayne.rerepack.workspace.compile.optifine.OptifineCompileTarget;
import org.crayne.rerepack.workspace.parse.RePackParserSpecification;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Main {

    public static void main(@NotNull final String... args) {
        VanillaItems.loadVanillaItems();

        final File workspaceDirectory = new File("sample");
        final File outputDirectory = new File("sample-out");

        final ExpressionParser parser = RePackParserSpecification.INSTANCE.parser();
        final Workspace workspace = WorkspaceBuilder.of(parser, workspaceDirectory);

        final OptifineCompileTarget compileTarget = new OptifineCompileTarget(outputDirectory);
        compileTarget.compile(workspace);
    }

}
