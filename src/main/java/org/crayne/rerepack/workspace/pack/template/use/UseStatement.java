package org.crayne.rerepack.workspace.pack.template.use;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.DefinitionException;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.pack.template.Template;
import org.crayne.rerepack.workspace.pack.template.TemplateContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.USE_PARAM_SPEC;

public class UseStatement implements Parseable, Initializable {

    @NotNull
    private final Token identifier;

    @NotNull
    private final DefinitionContainer givenParameters;

    public UseStatement(@NotNull final Token identifier, @NotNull final DefinitionContainer givenParameters) {
        this.identifier = identifier;
        this.givenParameters = givenParameters;
    }

    public void initialize() throws WorkspaceException {
        givenParameters.initialize();
    }

    public void applyTo(@NotNull final PackScope packScope,
                        @NotNull final TemplateContainer templateContainer) throws WorkspaceException {
        final Template template = templateContainer.definition(identifier);
        template.applyTemplate(packScope, this, templateContainer);
    }

    public void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws WorkspaceException {
        for (final Node useParameterSpecification : ast.children(USE_PARAM_SPEC)) {
            parseParameterSpecification(useParameterSpecification);
        }
    }

    private void parseParameterSpecification(@NotNull final Node node) throws DefinitionException {
        final Token identifier = node.child(0).value();
        final Token value = node.child(2).valueClean();
        assert identifier != null;

        parseParameterSpecification(identifier, value);
    }

    private void parseParameterSpecification(@NotNull final Token identifier, @NotNull final Token value) throws DefinitionException {
        givenParameters.createDefinition(identifier, value);
    }

    @NotNull
    public Token identifier() {
        return identifier;
    }

    @NotNull
    public DefinitionContainer givenParameters() {
        return givenParameters;
    }

    @NotNull
    public String toString() {
        return "UseStatement{" +
                "identifier=" + identifier +
                ", givenParameters=" + givenParameters +
                '}';
    }

}
