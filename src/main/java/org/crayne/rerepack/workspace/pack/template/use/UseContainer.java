package org.crayne.rerepack.workspace.pack.template.use;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.pack.template.TemplateContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.USE_STATEMENT;

public class UseContainer implements Parseable, Initializable {

    @NotNull
    private final Set<UseStatement> useStatements;

    @NotNull
    private final DefinitionContainer definitionContainer;

    public UseContainer(@NotNull final DefinitionContainer definitionContainer) {
        this.definitionContainer = definitionContainer;
        this.useStatements = new HashSet<>();
    }

    public void initialize() throws WorkspaceException {
        for (final UseStatement useStatement : useStatements) useStatement.initialize();
    }

    public void applyAll(@NotNull final PackScope packScope,
                         @NotNull final TemplateContainer templateContainer) throws WorkspaceException {

        for (final UseStatement useStatement : useStatements)
            useStatement.applyTo(packScope, templateContainer);
    }

    public void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws WorkspaceException {
        for (final Node useStatementAST : ast.children(USE_STATEMENT)) {
            final Token identifier = useStatementAST.child(0).value();
            assert identifier != null;

            final UseStatement useStatement = createUseStatement(identifier, new DefinitionContainer(definitionContainer));
            useStatement.parseFromAST(useStatementAST.child(2), packScope);
        }
    }

    @NotNull
    public UseStatement createUseStatement(@NotNull final Token identifier,
                                   @NotNull final DefinitionContainer parameters) {
        final UseStatement useStatement = new UseStatement(identifier, parameters);
        addUseStatement(useStatement);
        return useStatement;
    }

    public void addUseStatement(@NotNull final UseStatement useStatement) {
        useStatements.add(useStatement);
    }

    @NotNull
    public Set<UseStatement> useStatements() {
        return Collections.unmodifiableSet(useStatements);
    }

    @NotNull
    public String toString() {
        return "UseContainer{" +
                "useStatements=" + useStatements +
                ", definitionContainer=" + definitionContainer +
                '}';
    }

}
