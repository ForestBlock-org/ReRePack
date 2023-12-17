package org.crayne.rerepack.workspace.pack.lang;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackScope;
import org.crayne.rerepack.workspace.pack.definition.Definition;
import org.crayne.rerepack.workspace.pack.definition.DefinitionContainer;
import org.crayne.rerepack.workspace.parse.parseable.Initializable;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.crayne.rerepack.workspace.predicate.TokenPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.LANG_SINGLE_EXPRESSION;
import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.LANG_SINGLE_REPLACEMENT;

public class LangStatement implements Parseable, Initializable {

    @NotNull
    private final Set<TokenPredicate> replacements;

    @NotNull
    private final Set<Token> languageFileMatches;

    @NotNull
    private final DefinitionContainer definitionContainer;

    public LangStatement(@NotNull final DefinitionContainer definitionContainer) {
        this.definitionContainer = definitionContainer;
        this.replacements = new HashSet<>();
        this.languageFileMatches = new HashSet<>();
    }

    @NotNull
    public Set<TokenPredicate> replacements() {
        return replacements;
    }

    @NotNull
    public Set<Token> languageFileMatches() {
        return languageFileMatches;
    }

    public void initialize() throws WorkspaceException {
        final Set<Token> languageFileMatchesInitialized = new HashSet<>();
        for (final Token t : languageFileMatches)
            languageFileMatchesInitialized.add(Definition.parseValueByDefinitions(t, definitionContainer));

        languageFileMatches.clear();
        languageFileMatches.addAll(languageFileMatchesInitialized);

        for (final TokenPredicate replace : replacements)
            Definition.initializeDefinition(replace, definitionContainer);
    }

    private void parseLanguageMatches(@NotNull final Node languageFileScope) {
        languageFileScope.children(LANG_SINGLE_EXPRESSION)
                .stream()
                .map(n -> n.child(0))
                .map(Node::valueClean)
                .forEach(languageFileMatches::add);
    }

    private void parseLanguageReplacements(@NotNull final Node languageReplaceScope) {
        languageReplaceScope.children(LANG_SINGLE_REPLACEMENT)
                .stream()
                .map(LangStatement::parseDefaultPredicate)
                .forEach(replacements::add);
    }

    @NotNull
    private static TokenPredicate parseDefaultPredicate(@NotNull final Node node) {
        final Token key = node.child(0).valueClean();
        final Token value = node.child(2).valueClean();

        return new TokenPredicate(key, value);
    }

    public void parseFromAST(@NotNull final Node ast, @NotNull final PackScope packScope) throws WorkspaceException {
        final Node languageFileScope = ast.child(2);
        final Node languageReplaceScope = ast.child(6);

        parseLanguageMatches(languageFileScope);
        parseLanguageReplacements(languageReplaceScope);
    }

    @NotNull
    public String toString() {
        return "LangStatement{" +
                "replacements=" + replacements +
                ", languageFileMatches=" + languageFileMatches +
                ", definitionContainer=" + definitionContainer +
                '}';
    }
}
