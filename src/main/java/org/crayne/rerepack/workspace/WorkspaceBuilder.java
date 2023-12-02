package org.crayne.rerepack.workspace;

import org.apache.commons.io.FileUtils;
import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.syntax.parser.except.ParserException;
import org.crayne.rerepack.syntax.parser.ExpressionParser;
import org.crayne.rerepack.util.logging.Logger;
import org.crayne.rerepack.util.logging.LoggingLevel;
import org.crayne.rerepack.util.logging.message.TraceBackMessage;
import org.crayne.rerepack.workspace.util.except.DefinitionException;
import org.crayne.rerepack.workspace.util.pack.Pack;
import org.crayne.rerepack.workspace.util.pack.PackException;
import org.crayne.rerepack.workspace.util.replacement.predicate.Predicate;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.crayne.rerepack.workspace.parse.RePackParserSpecification.*;

public class WorkspaceBuilder {

    @NotNull
    private final ExpressionParser parser;

    @NotNull
    private final Workspace workspace;

    @NotNull
    private final Logger logger;

    @NotNull
    private final File directory;

    @NotNull
    private final Map<Pack, List<String>> packContentMap;

    public WorkspaceBuilder(@NotNull final Logger logger, @NotNull final ExpressionParser parser, @NotNull final File directory) {
        this.parser = parser;
        this.workspace = new Workspace();
        this.packContentMap = new HashMap<>();
        this.directory = directory;
        this.logger = logger;
    }

    private void handleDefinitionError(@NotNull final Pack pack, @NotNull final DefinitionException e) {
        final Optional<Token> traceback = e.traceback();
        traceback.ifPresentOrElse(t ->
                        logger.log(TraceBackMessage.Builder
                                .createBuilder(e.getMessage(), LoggingLevel.DEFINITION_ERROR)
                                .positionInformation(t, packContentMap.get(pack))
                                .build()),
                  () -> logger.log(e.getMessage(), LoggingLevel.DEFINITION_ERROR));

        throw new DefinitionException(e);
    }

    public void readPackFiles() {
        final Map<Pack, Node> packNodeMap = new HashMap<>();
        final Iterator<File> packFileIterator = FileUtils.iterateFiles(directory, new String[]{"rpk"}, true);

        try {
            while (packFileIterator.hasNext()) {
                final File packFile = packFileIterator.next();
                parsePackFile(packFile, packNodeMap);
            }
        } catch (final IOException | WorkspaceException | ParserException e) {
            logger.log(e.getMessage(), LoggingLevel.PARSING_ERROR);
            return;
        } catch (final PackException e) {
            logger.log(e.getMessage(), LoggingLevel.PACKING_ERROR);
            return;
        }
        try {
            packNodeMap.forEach(this::defineGlobal);
            packNodeMap.forEach(this::defineLocal);
        } catch (final DefinitionException e) {
            return;
        }
        try {
            packNodeMap.forEach(this::parseMatchReplacements);
        } catch (final DefinitionException e) {
            return;
        }
        /*System.out.println("global variables:");
        workspace.definitionContainer().definitions().forEach((key, value) ->
                System.out.println("global definition: " + key.token() + " = " + value.token()));

        packNodeMap.forEach((pack, node) -> {
            System.out.println("local definitions in" + pack.name() + ": ");
            pack.localDefinitionContainer().definitions().forEach((key, value) ->
                    System.out.println("    local definition: " + key.token() + " = " + value.token()));

            System.out.println("match statements:");
            pack.replacements().forEach(replacement -> {
                replacement.matches().forEach((key, value) ->
                        System.out.println("    required nbt: " + key.token() + " = " + value.token()));
                replacement.replacements().forEach((key, value) ->
                        System.out.println("    will replace: " + key.token() + " with " + value.token()));
            });
            System.out.println("-".repeat(24));
        });*/
    }

    @NotNull
    public String packName(@NotNull final File file) {
        final String workspacePath = directory.getAbsolutePath();
        final String packPath = file.getAbsolutePath();

        if (!packPath.startsWith(workspacePath))
            throw new WorkspaceException("Cannot read pack name of pack file outside of workspace");

        return packPath.substring(workspacePath.length() + 1).replace("/", ".");
    }

    @NotNull
    private static Predicate parseDefaultPredicate(@NotNull final Node node) {
        final Token key = node.child(0).valueClean();
        final Token value = node.child(2).valueClean();

        return new Predicate(key, value);
    }

    @NotNull
    private static Set<Predicate> parseIndividualItemPredicates(@NotNull final Node replacementExpressions) {
        return replacementExpressions.children(ITEMS_STATEMENT_INDIVIDUAL)
                .stream()
                .map(n -> n.child(2))
                .map(WorkspaceBuilder::parseSingleItemSetPredicate)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Set<Predicate> parseSingleItemSetPredicate(@NotNull final Node node) {
        return node.children(ITEM_SINGLE_SET_PREDICATE)
                .stream()
                .map(WorkspaceBuilder::parseDefaultPredicate)
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Set<Predicate> parseSetallItemPredicates(@NotNull final Node replacementExpressions) {
        return replacementExpressions.children(ITEMS_STATEMENT_SETALL)
                .stream()
                .map(n -> {
                    final Token value = n.child(5).valueClean();
                    return parseSingleItemIdentifier(n.child(2), value);
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Set<Predicate> parseSingleItemIdentifier(@NotNull final Node node, @NotNull final Token value) {
        return node.children(ITEM_SINGLE_IDENTIFIER)
                .stream()
                .map(singleNode -> new Predicate(singleNode.child(0).valueClean(), value))
                .collect(Collectors.toSet());
    }

    private static void parseMatchStatement(@NotNull final Pack pack, @NotNull final Node node) {
        final Set<Predicate> matches = parseMatches(node.child(2));
        final Set<Predicate> replacements = parseReplacements(node.child(6));

        pack.createReplacement(matches, replacements);
    }

    @NotNull
    private static Set<Predicate> parseMatches(@NotNull final Node node) {
        final List<Node> matchExpressions = node.children(SINGLE_MATCH_EXPRESSION);

        return matchExpressions.stream()
                .map(WorkspaceBuilder::parseDefaultPredicate)
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Set<Predicate> parseReplacements(@NotNull final Node node) {
        final Set<Predicate> replacements = new HashSet<>();

        replacements.addAll(parseIndividualItemPredicates(node));
        replacements.addAll(parseSetallItemPredicates(node));
        return replacements;
    }

    private void parseMatchReplacements(@NotNull final Pack pack, @NotNull final Node packAST) {
        try {
            packAST.children(MATCH_STATEMENT)
                    .forEach(n -> parseMatchStatement(pack, n));
        } catch (final DefinitionException e) {
            handleDefinitionError(pack, e);
        }
    }

    private void define(@NotNull final Pack pack, @NotNull final Node node,
                        @NotNull final BiConsumer<Token, Token> definitionConsumer) {
        final Token identifier = node.child(1).value();
        final Token value = node.child(3).valueClean();
        assert identifier != null;

        try {
            definitionConsumer.accept(identifier, value);
        } catch (final DefinitionException e) {
            handleDefinitionError(pack, e);
        }
    }

    private void defineGlobal(@NotNull final Pack pack, @NotNull final Node packAST) {
        try {
            packAST.children(GLOBAL_DEFINITION_STATEMENT)
                    .forEach(node -> define(pack, node, workspace::createDefinition));

            workspace.initializeDefinitions();
        } catch (final DefinitionException e) {
            handleDefinitionError(pack, e);
        }
    }

    private void defineLocal(@NotNull final Pack pack, @NotNull final Node packAST) {
        try {
            packAST.children(DEFINITION_STATEMENT)
                    .forEach(node -> define(pack, node, pack::createLocalDefinition));

            pack.initializeLocalDefinitions();
        } catch (final DefinitionException e) {
            handleDefinitionError(pack, e);
        }
    }

    private void parsePackFile(@NotNull final File file,
                               @NotNull final Map<Pack, Node> packNodeMap) throws IOException {
        parsePackFile(packName(file), file, packNodeMap);
    }

    private void parsePackFile(@NotNull final String name, @NotNull final File file,
                               @NotNull final Map<Pack, Node> packNodeMap) throws IOException {
        final Pack pack = workspace.createPackage(name);
        final List<String> content = new ArrayList<>();

        final Node packAST = parser.parse(file, content)
                .orElseThrow(() -> new WorkspaceException("Could not parse pack file '" + name + "'"));

        packNodeMap.put(pack, packAST);
        packContentMap.put(pack, content);
    }

    @NotNull
    public ExpressionParser parser() {
        return parser;
    }

}
