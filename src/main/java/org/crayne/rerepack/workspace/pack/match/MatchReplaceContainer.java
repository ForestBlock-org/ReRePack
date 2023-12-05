package org.crayne.rerepack.workspace.pack.match;

import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.MATCH_STATEMENT;

public class MatchReplaceContainer implements Parseable, Initializable {

    @NotNull
    private final Set<MatchReplaceStatement> matchesReplacements;

    @NotNull
    private final DefinitionContainer definitionContainer;

    public MatchReplaceContainer(@NotNull final DefinitionContainer definitionContainer) {
        this.definitionContainer = definitionContainer;
        this.matchesReplacements = new HashSet<>();
    }

    @NotNull
    public MatchReplaceStatement createMatchReplaceStatement() {
        final MatchReplaceStatement matchReplaceStatement = new MatchReplaceStatement(definitionContainer);
        addMatchReplaceStatement(matchReplaceStatement);
        return matchReplaceStatement;
    }

    public void addMatchReplaceStatement(@NotNull final MatchReplaceStatement matchReplaceStatement) {
        matchesReplacements.add(matchReplaceStatement);
    }

    @NotNull
    public Set<MatchReplaceStatement> matchesReplacements() {
        return Collections.unmodifiableSet(matchesReplacements);
    }

    public void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws WorkspaceException {
        for (final Node matchReplacementAST : ast.children(MATCH_STATEMENT)) {
            final MatchReplaceStatement matchReplaceStatement = createMatchReplaceStatement();
            matchReplaceStatement.parseFromAST(matchReplacementAST, packScope);
        }
    }

    public void initialize() throws WorkspaceException {
        for (final MatchReplaceStatement statement : matchesReplacements) {
            statement.initialize();
        }
    }

    @NotNull
    public String toString() {
        return "MatchReplaceContainer{" +
                "matchesReplacements=" + matchesReplacements +
                ", definitionContainer=" + definitionContainer +
                '}';
    }

}
