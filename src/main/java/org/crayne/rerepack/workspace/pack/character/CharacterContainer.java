package org.crayne.rerepack.workspace.pack.character;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.CHAR_STATEMENT;

public class CharacterContainer implements Parseable, Initializable {

    @NotNull
    private final Set<CharacterStatement> characterStatements;

    @NotNull
    private final DefinitionContainer parentContainer;

    public CharacterContainer(@NotNull final DefinitionContainer parentContainer) {
        this.characterStatements = new HashSet<>();
        this.parentContainer = parentContainer;
    }

    @NotNull
    public DefinitionContainer parentContainer() {
        return parentContainer;
    }

    @NotNull
    public Set<CharacterStatement> characterStatements() {
        return characterStatements;
    }

    @NotNull
    public CharacterStatement createCharacterStatement(@NotNull final Token charsToReplace,
                                                       @NotNull final Token bitmapFilePath) {

        final CharacterStatement characterStatement = new CharacterStatement(parentContainer,
                charsToReplace, bitmapFilePath);

        addCharacterStatement(characterStatement);
        return characterStatement;
    }

    public void addCharacterStatement(@NotNull final CharacterStatement characterStatement) {
        characterStatements.add(characterStatement);
    }

    public void initialize() throws WorkspaceException {
        for (final CharacterStatement characterStatement : characterStatements)
            characterStatement.initialize();
    }

    public void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws WorkspaceException {
        for (final Node characterStatementNode : ast.children(CHAR_STATEMENT)) {
            final Token charsToReplace = characterStatementNode.child(1).valueClean();
            final Token bitmapFilePath = characterStatementNode.child(2).valueClean();
            final Node replacementScope = characterStatementNode.child(4);

            final CharacterStatement characterStatement = createCharacterStatement(charsToReplace, bitmapFilePath);
            characterStatement.parseFromAST(replacementScope, packScope);
        }
    }

}
