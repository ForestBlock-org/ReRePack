package org.crayne.rerepack.workspace.pack.lang;

import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.LANG_STATEMENT;

public class LangContainer implements Parseable, Initializable {

    @NotNull
    private final Set<LangStatement> langStatements;

    @NotNull
    private final DefinitionContainer definitionContainer;

    public LangContainer(@NotNull final DefinitionContainer parentContainer) {
        this.definitionContainer = new DefinitionContainer(parentContainer);
        this.langStatements = new HashSet<>();
    }

    @NotNull
    public Set<LangStatement> langStatements() {
        return langStatements;
    }

    @NotNull
    public LangStatement createLangStatement() {
        final LangStatement langStatement = new LangStatement(definitionContainer);
        addLangStatement(langStatement);
        return langStatement;
    }

    public void addLangStatement(@NotNull final LangStatement langStatement) {
        langStatements.add(langStatement);
    }

    public void initialize() throws WorkspaceException {
        for (final LangStatement langStatement : langStatements)
            langStatement.initialize();
    }

    public void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws WorkspaceException {
        for (final Node langStatementAST : ast.children(LANG_STATEMENT)) {
            final LangStatement langStatement = createLangStatement();
            langStatement.parseFromAST(langStatementAST, packScope);
        }
    }

    @NotNull
    public String toString() {
        return "LangContainer{" +
                "langStatements=" + langStatements +
                ", definitionContainer=" + definitionContainer +
                '}';
    }

}
