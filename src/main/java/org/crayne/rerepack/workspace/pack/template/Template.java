package org.crayne.rerepack.workspace.pack.template;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.DefinitionException;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.pack.match.MatchReplaceContainer;
import org.crayne.rerepack.workspace.pack.write.WriteContainer;
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

    public Template(@NotNull final Token identifier, @NotNull final Map<Token, Optional<Token>> parameters) {
        this.identifier = identifier;
        this.parameters = new HashMap<>(parameters);
        this.definitionContainer = new DefinitionContainer();
        this.matchReplaceContainer = new MatchReplaceContainer();
        this.writeContainer = new WriteContainer();
    }

    public Template() {
        this.identifier = null;
        this.parameters = new HashMap<>();
        this.definitionContainer = new DefinitionContainer();
        this.matchReplaceContainer = new MatchReplaceContainer();
        this.writeContainer = new WriteContainer();
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
    public WriteContainer writeContainer() {
        return writeContainer;
    }

    @NotNull
    public MatchReplaceContainer matchReplaceContainer() {
        return matchReplaceContainer;
    }

    @NotNull
    public Map<Token, Optional<Token>> parameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void parseFromAST(@NotNull final Node ast) throws WorkspaceException {
        final Token templateIdentifier = ast.child(1).value();
        assert templateIdentifier != null;

        final Node templateParametersNode = ast.child(3);
        final Node templateScope = ast.child(6);

        final Map<Token, Optional<Token>> templateParameters = parseTemplateParameters(templateParametersNode);
        identifier = templateIdentifier;
        parameters.putAll(templateParameters);

        definitionContainer().parseFromAST(templateScope);
        matchReplaceContainer().parseFromAST(templateScope);
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
                throw new DefinitionException("Duplicate template " + "parameter '" + identifier + "'", identifier);

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

}
