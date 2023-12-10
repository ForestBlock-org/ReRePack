package org.crayne.rerepack.workspace.pack.template;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.DefinitionException;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.character.CharacterContainer;
import org.crayne.rerepack.workspace.pack.character.CharacterStatement;
import org.crayne.rerepack.workspace.pack.definition.Definition;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.pack.match.MatchReplaceContainer;
import org.crayne.rerepack.workspace.pack.match.MatchReplaceStatement;
import org.crayne.rerepack.workspace.pack.template.use.UseContainer;
import org.crayne.rerepack.workspace.pack.template.use.UseStatement;
import org.crayne.rerepack.workspace.pack.write.WriteContainer;
import org.crayne.rerepack.workspace.pack.write.WriteStatement;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.TEMPLATE_PARAM_SPEC_REQUIRED;
import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.TEMPLATE_PARAM_SPEC_WITH_DEFAULT;

public class Template implements PackScope, Parseable {

    @Nullable
    private Token identifier;

    @NotNull
    private final Map<Token, Optional<Token>> parameters;

    @NotNull
    private final DefinitionContainer definitionContainer;

    @NotNull
    private final MatchReplaceContainer matchReplaceContainer;

    @NotNull
    private final WriteContainer writeContainer;

    @NotNull
    private final UseContainer useContainer;

    @NotNull
    private final CharacterContainer characterContainer;

    public Template(@NotNull final Token identifier,
                    @NotNull final DefinitionContainer parentContainer,
                    @NotNull final Map<Token, Optional<Token>> parameters) throws DefinitionException {
        this.identifier = identifier;
        this.parameters = new HashMap<>(parameters);
        this.definitionContainer = new DefinitionContainer(parentContainer);
        this.matchReplaceContainer = new MatchReplaceContainer(definitionContainer);
        this.writeContainer = new WriteContainer(definitionContainer);
        this.useContainer = new UseContainer(definitionContainer);
        this.characterContainer = new CharacterContainer(definitionContainer);

        declareParameters();
    }

    public Template(@NotNull final DefinitionContainer parentContainer) {
        this.identifier = null;
        this.parameters = new HashMap<>();
        this.definitionContainer = new DefinitionContainer(parentContainer);
        this.matchReplaceContainer = new MatchReplaceContainer(definitionContainer);
        this.writeContainer = new WriteContainer(definitionContainer);
        this.useContainer = new UseContainer(definitionContainer);
        this.characterContainer = new CharacterContainer(definitionContainer);
    }

    private void declareParameters() throws DefinitionException {
        for (final Token p : parameters.keySet()) {
            definitionContainer.createDefinition(p, Token.of("$(" + p.token() + ")", p));

            final Optional<Token> defaultValue = parameters.get(p);
            if (defaultValue.isEmpty()) continue;
            Definition.ensureValidDefinition(defaultValue.get(), definitionContainer);
        }
    }

    public void applyTemplate(@NotNull final PackScope usedIn,
                              @NotNull final UseStatement useStatement,
                              @NotNull final TemplateContainer templateContainer) throws WorkspaceException {
        applyTemplate(usedIn, useStatement.givenParameters(), useStatement.identifier(), templateContainer);
    }

    public void applyTemplate(@NotNull final PackScope usedIn,
                              @NotNull final DefinitionContainer givenParameters,
                              @NotNull final Token calledAt,
                              @NotNull final TemplateContainer templateContainer) throws WorkspaceException {
        final DefinitionContainer temporaryContainer = new DefinitionContainer(usedIn.definitionContainer());
        putAllParameters(temporaryContainer, givenParameters, calledAt);

        applyMatchStatements(usedIn, temporaryContainer);
        applyWriteStatements(usedIn, temporaryContainer);
        applyCharacterStatements(usedIn, temporaryContainer);
        useContainer.applyAll(usedIn, templateContainer);
    }

    private void handleMissingParameter(@NotNull final Token ident,
                                        @NotNull final Token calledAt,
                                        @NotNull final DefinitionContainer givenParameters) throws DefinitionException {
        if (givenParameters.definitions().containsKey(ident)) return;

        final Optional<Token> defaultParamter = parameters.get(ident);
        if (defaultParamter.isPresent()) {
            givenParameters.createDefinition(ident, defaultParamter.get());
            return;
        }

        throw new DefinitionException("Cannot use template '" + identifier + "'," +
                " missing parameter: " + ident, calledAt, ident);
    }

    private void handleRedundantParameters(@NotNull final Token calledAt,
                                           @NotNull final List<Token> redundantParameters) throws DefinitionException {
        if (redundantParameters.isEmpty()) return;

        redundantParameters.add(calledAt); // for error traceback purposes, nothing else
        throw new DefinitionException("Cannot use template '" + identifier + "'," +
                " redundant parameters: " + redundantParameters, redundantParameters);
    }

    private void putAllParameters(@NotNull final DefinitionContainer temporaryContainer,
                                  @NotNull final DefinitionContainer givenParameters,
                                  @NotNull final Token calledAt) throws DefinitionException {
        final List<Token> redundantGivenParameters = new ArrayList<>(givenParameters.definitions().keySet());

        for (final Token ident : definitionContainer().definitions().keySet()) {
            if (!parameters.containsKey(ident)) {
                temporaryContainer.addDefinition(ident, definitionContainer().definition(ident));
                continue;
            }
            handleMissingParameter(ident, calledAt, givenParameters);

            final Token givenValue = givenParameters.definition(ident).fullValue();
            temporaryContainer.createDefinition(ident, givenValue);
            redundantGivenParameters.remove(ident);
        }
        handleRedundantParameters(calledAt, redundantGivenParameters);
    }

    private void applyMatchStatements(@NotNull final PackScope usedIn,
                                      @NotNull final DefinitionContainer temporaryContainer) {
        for (final MatchReplaceStatement matchReplaceStatement : matchReplaceContainer().matchesReplacements()) {
            final MatchReplaceStatement finalizedStatement = new MatchReplaceStatement(temporaryContainer,
                    matchReplaceStatement.matches(), matchReplaceStatement.replacements());

            usedIn.matchReplaceContainer().addMatchReplaceStatement(finalizedStatement);
        }
    }

    private void applyWriteStatements(@NotNull final PackScope usedIn,
                                      @NotNull final DefinitionContainer temporaryContainer) {
        for (final WriteStatement writeStatement : writeContainer().writeStatements()) {
            final WriteStatement finalizedStatement = new WriteStatement(temporaryContainer,
                    writeStatement.destinationPath(), writeStatement.lines());

            usedIn.writeContainer().addWriteStatement(finalizedStatement);
        }
    }

    private void applyCharacterStatements(@NotNull final PackScope usedIn,
                                          @NotNull final DefinitionContainer temporaryContainer) throws DefinitionException {
        for (final CharacterStatement characterStatement : characterContainer().characterStatements()) {
            final CharacterStatement finalizedStatement = new CharacterStatement(temporaryContainer,
                    characterStatement.characters(), characterStatement.bitmapFilePath(),
                    characterStatement.definitionContainer());

            usedIn.characterContainer().addCharacterStatement(finalizedStatement);
        }
    }

    @NotNull
    public Optional<Token> identifier() {
        return Optional.ofNullable(identifier);
    }

    @NotNull
    public DefinitionContainer definitionContainer() {
        return definitionContainer;
    }

    @NotNull
    public CharacterContainer characterContainer() {
        return characterContainer;
    }

    @NotNull
    public WriteContainer writeContainer() {
        return writeContainer;
    }

    @NotNull
    public MatchReplaceContainer matchReplaceContainer() {
        return matchReplaceContainer;
    }

    @NotNull
    public UseContainer useContainer() {
        return useContainer;
    }

    @NotNull
    public Map<Token, Optional<Token>> parameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws WorkspaceException {
        final Token templateIdentifier = ast.child(1).value();
        assert templateIdentifier != null;

        final Node templateParametersNode = ast.child(3);
        final Node templateScope = ast.child(6);

        final Map<Token, Optional<Token>> templateParameters = parseTemplateParameters(templateParametersNode);
        identifier = templateIdentifier;
        parameters.putAll(templateParameters);
        declareParameters();

        definitionContainer().parseFromAST(templateScope, packScope);
        matchReplaceContainer().parseFromAST(templateScope, packScope);
        writeContainer().parseFromAST(templateScope, packScope);
        characterContainer().parseFromAST(templateScope, packScope);
    }

    private void parseDefaultTemplateParameters(@NotNull final Node templateParameters,
                                                @NotNull final Map<Token, Optional<Token>> result) throws DefinitionException {

        for (final Node parameter : templateParameters.children(TEMPLATE_PARAM_SPEC_WITH_DEFAULT)) {
            final Token identifier = parameter.child(0).value();
            final Token defaultValue = parameter.child(2).valueClean();
            assert identifier != null;

            if (result.containsKey(identifier))
                throw new DefinitionException("Duplicate template " +
                        "parameter '" + identifier + "'", identifier);

            result.put(identifier, Optional.of(defaultValue));
        }
    }

    private void parseRequiredTemplateParameters(@NotNull final Node templateParameters,
                                                 @NotNull final Map<Token, Optional<Token>> result) throws DefinitionException {
        final List<Token> requiredParameters = templateParameters
                .children(TEMPLATE_PARAM_SPEC_REQUIRED)
                .stream()
                .map(n -> n.child(1).value())
                .filter(Objects::nonNull)
                .toList();

        for (final Token identifier : requiredParameters) {
            if (result.containsKey(identifier))
                throw new DefinitionException("Duplicate template parameter '" + identifier + "'", identifier);

            result.put(identifier, Optional.empty());
        }
    }

    @NotNull
    private Map<Token, Optional<Token>> parseTemplateParameters(@NotNull final Node templateParameters) throws DefinitionException {
        final Map<Token, Optional<Token>> result = new HashMap<>();

        parseRequiredTemplateParameters(templateParameters, result);
        parseDefaultTemplateParameters(templateParameters, result);

        return result;
    }

    @NotNull
    public String toString() {
        return "Template{" +
                "identifier=" + identifier +
                ", parameters=" + parameters +
                ", definitionContainer=" + definitionContainer +
                ", matchReplaceContainer=" + matchReplaceContainer +
                ", writeContainer=" + writeContainer +
                ", useContainer=" + useContainer +
                '}';
    }

}
