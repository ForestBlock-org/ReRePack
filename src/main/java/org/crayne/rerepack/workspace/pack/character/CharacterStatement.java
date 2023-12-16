package org.crayne.rerepack.workspace.pack.character;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.compile.optifine.util.ImageSplit;
import org.crayne.rerepack.workspace.except.DefinitionException;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.container.MapContainer;
import org.crayne.rerepack.workspace.pack.definition.Definition;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.CHAR_SINGLE_STATEMENT;

public class CharacterStatement implements Parseable, Initializable {

    @NotNull
    private Token characters;

    @NotNull
    private Token bitmapFilePath;

    @NotNull
    private final DefinitionContainer definitionContainer;

    public CharacterStatement(@NotNull final MapContainer<Definition> definitionContainer,
                              @NotNull final Token characters, @NotNull final Token bitmapFilePath) {
        this.characters = characters;
        this.bitmapFilePath = bitmapFilePath;
        this.definitionContainer = new DefinitionContainer(definitionContainer);
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
    public CharacterStatement createSplit(@NotNull final ImageSplit imageSplit,
                                          @NotNull final String textureFilePath,
                                          final int initialHeight,
                                          final int scaledSplitSize) {
        final List<String> newCharacterList = characterList()
                .stream()
                .map(s -> String.valueOf((char) (s.charAt(0) + imageSplit.splitIndex())))
                .toList();

        final Token newCharacters = Token.of(String.join("", newCharacterList));
        final int ascent = ascent() - scaledSplitSize * imageSplit.row();
        final int originalHeight = height().orElse(initialHeight);

        final CharacterStatement characterStatement = new CharacterStatement(definitionContainer.parent().orElseThrow(),
                newCharacters, Token.of(textureFilePath));

        final DefinitionContainer derivativeContainer = characterStatement.definitionContainer;
        try {
            derivativeContainer.createDefinition(Token.of("ascent"),
                    Token.of("" + ascent));

            derivativeContainer.createDefinition(Token.of("height"),
                    Token.of("" + Math.min(originalHeight, scaledSplitSize)));
        } catch (final DefinitionException e) {
            throw new RuntimeException(e);
        }

        return characterStatement;
    }

    @Nullable
    public <T> T find(@NotNull final String key, @NotNull final Function<String, T> function,
                      @Nullable final T defaultValue) {
        return definitionContainer
                .findDefinition(Token.of(key))
                .map(Definition::fullValue)
                .map(Token::token)
                .map(function)
                .orElse(defaultValue);
    }

    @NotNull
    public Optional<Integer> height() {
        return Optional.ofNullable(find("height", s -> {
            try {
                return Integer.parseInt(s);
            } catch (final NumberFormatException e) {
                return null;
            }
        }, null));
    }

    public int ascent() {
        final Integer ascent = find("ascent", s -> {
            try {
                return Integer.parseInt(s);
            } catch (final NumberFormatException e) {
                return null;
            }
        }, 0);
        if (ascent == null) return 0;
        return ascent;
    }

    public double resolution() {
        final Double resolution = find("resolution", s -> {
            try {
                return Double.parseDouble(s);
            } catch (final NumberFormatException e) {
                return null;
            }
        }, 1.0d);
        if (resolution == null) return 0;
        return resolution;
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
