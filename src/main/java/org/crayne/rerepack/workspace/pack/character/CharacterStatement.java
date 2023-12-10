package org.crayne.rerepack.workspace.pack.character;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.DefinitionException;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.container.MapContainer;
import org.crayne.rerepack.workspace.pack.definition.Definition;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.CHAR_SINGLE_STATEMENT;

public class CharacterStatement implements Parseable, Initializable {

    @NotNull
    private Token characters;

    @NotNull
    private Token bitmapFilePath;

    @NotNull
    private final DefinitionContainer definitionContainer;

    public CharacterStatement(@NotNull final DefinitionContainer definitionContainer,
                              @NotNull final Token characters, @NotNull final Token bitmapFilePath) {
        this.characters = characters;
        this.bitmapFilePath = bitmapFilePath;
        this.definitionContainer = new DefinitionContainer(definitionContainer);
    }

    public CharacterStatement(@NotNull final DefinitionContainer definitionContainer,
                              @NotNull final Token characters, @NotNull final Token bitmapFilePath,
                              @NotNull final DefinitionContainer copyOtherDefinitionContainer) throws DefinitionException {
        this.characters = characters;
        this.bitmapFilePath = bitmapFilePath;
        this.definitionContainer = new DefinitionContainer(definitionContainer, copyOtherDefinitionContainer);
    }

    @NotNull
    public DefinitionContainer definitionContainer() {
        return definitionContainer;
    }

    @NotNull
    public Token characters() {
        return characters;
    }

    @NotNull
    public List<String> characterList() {
        return characters.token()
                .chars()
                .mapToObj(c -> String.valueOf((char) c))
                .toList();
    }

    @NotNull
    public Token bitmapFilePath() {
        return bitmapFilePath;
    }

    public void initialize() throws WorkspaceException {
        final MapContainer<Definition> parentContainer = definitionContainer.parent().orElseThrow();
        characters = Definition.parseValueByDefinitions(characters, parentContainer);
        bitmapFilePath = Definition.parseValueByDefinitions(bitmapFilePath, parentContainer);
        definitionContainer.initialize();
    }

    public void parseFromAST(@NotNull final Node charStatementAST, @NotNull final PackScope packScope) throws WorkspaceException {
        for (final Node charSingleStatement : charStatementAST.children(CHAR_SINGLE_STATEMENT)) {
            final Token key = charSingleStatement.child(0).valueClean();
            final Token value = charSingleStatement.child(2).valueClean();

            definitionContainer.createDefinition(key, value);
        }
    }

}
