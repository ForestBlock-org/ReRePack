package org.crayne.rerepack.workspace.pack.definition;

import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.DefinitionException;
import org.jetbrains.annotations.NotNull;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.GLOBAL_DEFINITION_STATEMENT;

public class GlobalDefinitionContainer extends DefinitionContainer {

    public GlobalDefinitionContainer() {
        super();
    }

    public void parseFromAST(@NotNull final Node ast) throws DefinitionException {
        parseFromAST(ast, GLOBAL_DEFINITION_STATEMENT);
    }

}
