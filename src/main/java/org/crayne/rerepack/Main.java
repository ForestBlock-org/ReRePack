package org.crayne.rerepack;

import org.crayne.rerepack.syntax.parser.ExpressionParser;
import org.crayne.rerepack.util.logging.Logger;
import org.crayne.rerepack.util.minecraft.VanillaItems;
import org.crayne.rerepack.workspace.Workspace;
import org.crayne.rerepack.workspace.WorkspaceBuilder;
import org.crayne.rerepack.workspace.compile.optifine.OptifineCompileTarget;
import org.crayne.rerepack.workspace.parse.RePackParserSpecification;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;

public class Main {

    public static void main(@NotNull final String... args) {
        final ExpressionParser parser = RePackParserSpecification.INSTANCE.parser();
        final Logger logger = parser.logger();

        if (args.length != 2) {
            logger.error("Expected 2 arguments for RePack, but got " + args.length);
            logger.info("Usage: java -jar RePack.jar 'input/directory/path' 'output/directory/path'");
            System.exit(1);
            return;
        }
        final File inputDirectory = new File(args[0]);
        final File outputDirectory = new File(args[1]);

        VanillaItems.loadVanillaItems();

        final Optional<Workspace> workspace = WorkspaceBuilder.of(parser, inputDirectory);
        if (workspace.isEmpty()) return;

        final OptifineCompileTarget compileTarget = new OptifineCompileTarget(outputDirectory);
        compileTarget.compile(workspace.get());
    }

}
