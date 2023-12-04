package org.crayne.rerepack.workspace.pack.template;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.container.MapContainer;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.TEMPLATE_STATEMENT;

public class TemplateContainer extends MapContainer<Template> implements Parseable {

    public TemplateContainer() {
        super();
    }

    @NotNull
    public Template createTemplate(@NotNull final Token identifier,
                                   @NotNull final Map<Token, Optional<Token>> parameters) throws WorkspaceException {
        return addDefinition(identifier, new Template(identifier, parameters));
    }

    public void parseFromAST(@NotNull final Node ast) throws WorkspaceException {
        for (final Node templateStatementAST : ast.children(TEMPLATE_STATEMENT)) {
            final Template template = new Template();
            template.parseFromAST(templateStatementAST);
            addDefinition(template.identifier().orElseThrow(() -> new WorkspaceException("Could not retrieve " +
                    "template identifier after parsing (report this bug!)")), template);
        }
    }

}
