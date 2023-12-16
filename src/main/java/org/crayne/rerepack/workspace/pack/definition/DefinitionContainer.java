package org.crayne.rerepack.workspace.pack.definition;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.DefinitionException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.container.MapContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.crayne.rerepack.workspace.predicate.TokenPredicate;
import org.jetbrains.annotations.NotNull;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.DEFINITION_STATEMENT;

public class DefinitionContainer extends MapContainer<Definition> implements Parseable, Initializable {

    public DefinitionContainer() {
        super();
    }

    public DefinitionContainer(@NotNull final MapContainer<Definition> parent) {
        super(parent);
    }

    @NotNull
    public Definition addDefinition(@NotNull final Token identifier, @NotNull final Definition definition) throws DefinitionException {
        return super.addDefinition(identifier, new Definition(definition.fullDefinition().createCopy()));
    }

    public void createDefinition(@NotNull final Token identifier, @NotNull final Token value) throws DefinitionException {
        addDefinition(identifier, new Definition(identifier, value));
    }

    @NotNull
    public Definition createDefinition(@NotNull final TokenPredicate definition) throws DefinitionException {
        return addDefinition(definition.key(), new Definition(definition));
    }

    protected void parseFromAST(@NotNull final Node ast, @NotNull final String branch) throws DefinitionException {
        for (final Node definition : ast.children(branch)) {
            final Token identifier = definition.child(1).value();
            final Token value = definition.child(3).valueClean();
            assert identifier != null;

            createDefinition(identifier, value);
        }
    }

    public void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws DefinitionException {
        parseFromAST(ast, DEFINITION_STATEMENT);
    }

    public void initialize() throws DefinitionException {
        for (final Definition definition : definitions().values())
            definition.initializeDefinition(this);

        if (parent().isEmpty()) return;
        for (final Definition definition : parent().get().definitions().values()) {
            definition.initializeDefinition(this);
            // use "this" definition container so that global variables can use local ones in their value (if needed)
        }
    }

}
