package org.crayne.rerepack.workspace.pack.template;

import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.Workspace;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.container.MapContainer;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.TEMPLATE_STATEMENT;

public class TemplateContainer extends MapContainer<Template> implements Parseable {

    @NotNull
    private final Workspace workspace;

    public TemplateContainer(@NotNull final Workspace workspace) {
        super();
        this.workspace = workspace;
    }

    @NotNull
    public String cannotRedefinePreviousDefinitionString() {
        return "Cannot redefine previously defined template";
    }

    @NotNull
    public String cannotFindDefinitionString() {
        return "Cannot find template";
    }

    public void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws WorkspaceException {
        for (final Node templateStatementAST : ast.children(TEMPLATE_STATEMENT)) {
            final Template template = new Template(packScope.definitionContainer(), workspace);
            template.parseFromAST(templateStatementAST, packScope);
            addDefinition(template.identifier().orElseThrow(() -> new WorkspaceException("Could not retrieve " +
                    "template identifier after parsing (report this bug!)")), template);
        }
    }

}
