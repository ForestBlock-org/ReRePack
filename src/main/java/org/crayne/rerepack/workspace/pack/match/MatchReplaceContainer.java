package org.crayne.rerepack.workspace.pack.match;

import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.MATCH_STATEMENT;

public class MatchReplaceContainer implements Parseable {

    @NotNull
    private final Set<MatchReplaceStatement> matchesReplacements;

    public MatchReplaceContainer() {
        this.matchesReplacements = new HashSet<>();
    }

    @NotNull
    public MatchReplaceStatement createMatchReplaceStatement() {
        final MatchReplaceStatement matchReplaceStatement = new MatchReplaceStatement();
        matchesReplacements.add(matchReplaceStatement);
        return matchReplaceStatement;
    }

    @NotNull
    public Set<MatchReplaceStatement> matchesReplacements() {
        return Collections.unmodifiableSet(matchesReplacements);
    }

    public void parseFromAST(@NotNull final Node ast) throws WorkspaceException {
        for (final Node matchReplacementAST : ast.children(MATCH_STATEMENT)) {
            final MatchReplaceStatement matchReplaceStatement = createMatchReplaceStatement();
            matchReplaceStatement.parseFromAST(matchReplacementAST);
        }
    }

}
