package org.crayne.rerepack;

import org.crayne.rerepack.syntax.parser.ExpressionParser;
import org.crayne.rerepack.util.logging.Logger;
import org.crayne.rerepack.util.minecraft.VanillaItem;
import org.crayne.rerepack.workspace.WorkspaceBuilder;
import org.crayne.rerepack.workspace.parse.RePackParserSpecification;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Main {

    public static void main(@NotNull final String... args) {
        VanillaItem.loadVanillaItems();

        final File workspaceDirectory = new File("sample");

        final Logger logger = new Logger();
        final ExpressionParser parser = RePackParserSpecification.INSTANCE.parser();
        final WorkspaceBuilder builder = new WorkspaceBuilder(logger, parser, workspaceDirectory);
        builder.readPackFiles();
    }

}
