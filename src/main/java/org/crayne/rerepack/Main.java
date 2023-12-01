package org.crayne.rerepack;

import org.crayne.rerepack.syntax.parser.ExpressionParser;
import org.crayne.rerepack.util.logging.Logger;
import org.crayne.rerepack.workspace.parse.RePackLexerSpecification;
import org.crayne.rerepack.workspace.parse.RePackParserSpecification;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Main {

    public static void main(@NotNull final String... args) throws IOException {
        // TODO finish parser rewrite so WorkspaceBuilder can work again
        //final WorkspaceBuilder builder = new WorkspaceBuilder(new Logger(), RePackParserSpecification.INSTANCE.parser(), new File("sample"));
        //builder.readPackFiles();
        final Logger logger = new Logger();
        final ExpressionParser parser = new ExpressionParser(RePackParserSpecification.INSTANCE.parentScopeDefinition(), logger, RePackLexerSpecification.INSTANCE);

        final File testFile = new File("test.rpk");
        final List<String> content = Files.readAllLines(testFile.toPath());
        parser.parse(testFile, content);
    }

}
