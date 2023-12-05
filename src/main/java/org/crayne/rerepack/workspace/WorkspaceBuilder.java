package org.crayne.rerepack.workspace;

import org.apache.commons.io.FileUtils;
import org.crayne.rerepack.syntax.ast.Node;
import org.crayne.rerepack.syntax.parser.ExpressionParser;
import org.crayne.rerepack.syntax.parser.except.ParserException;
import org.crayne.rerepack.util.logging.Logger;
import org.crayne.rerepack.util.logging.LoggingLevel;
import org.crayne.rerepack.util.logging.message.PositionInformationMessage;
import org.crayne.rerepack.workspace.except.DefinitionException;
import org.crayne.rerepack.workspace.except.WorkspaceException;
import org.crayne.rerepack.workspace.pack.PackFile;
import org.crayne.rerepack.workspace.parse.parseable.Parseable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
    private final Map<PackFile, List<String>> packContentMap;

    public WorkspaceBuilder(@NotNull final Logger logger, @NotNull final ExpressionParser parser, @NotNull final File directory) {
        this.parser = parser;
        this.workspace = new Workspace();
        this.packContentMap = new HashMap<>();
        this.directory = directory;
        this.logger = logger;
    }

    public void readPackFiles() {
        final Map<PackFile, Node> packNodeMap = new HashMap<>();
        final Iterator<File> packFileIterator = FileUtils.iterateFiles(directory,
                new String[] {"rpk"}, true);

        try {
            while (packFileIterator.hasNext()) {
                final File packFile = packFileIterator.next();
                parsePackFile(packFile, packNodeMap);
            }
        } catch (final IOException | ParserException e) {
            logger.log(e.getMessage(), LoggingLevel.PARSING_ERROR);
            return;
        } catch (final WorkspaceException e) {
            logger.log(e.getMessage(), LoggingLevel.WORKSPACE_ERROR);
            return;
        }
        parseAllFromAST(packNodeMap, packFile -> workspace.globalDefinitionContainer());
        parseAllFromAST(packNodeMap, packFile -> workspace.templateContainer());
        parseAllFromAST(packNodeMap, PackFile::definitionContainer);
        parseAllFromAST(packNodeMap, PackFile::matchReplaceContainer);

        forEachPackFile(packNodeMap, (packFile, node) -> {
            try {
                packFile.definitionContainer().initializeDefinitions();
            } catch (final DefinitionException e) {
                handleWorkspaceError(packFile, e);
            }
        });
    }

    private void parseAllFromAST(@NotNull final Map<PackFile, Node> packFileNodeMap,
                                 @NotNull final Function<PackFile, Parseable> parseableFunction) {
        forEachPackFile(packFileNodeMap, (packFile, ast) -> {
            try {
                parseableFunction.apply(packFile).parseFromAST(ast);
            } catch (final WorkspaceException e) {
                handleWorkspaceError(packFile, e);
            }
        });
    }

    private void forEachPackFile(@NotNull final Map<PackFile, Node> packFileNodeMap,
                                 @NotNull final BiConsumer<PackFile, Node> packFileNodeBiConsumer) {
        for (final Map.Entry<PackFile, Node> packFileNodeEntry : packFileNodeMap.entrySet()) {
            final PackFile packFile = packFileNodeEntry.getKey();
            final Node ast = packFileNodeEntry.getValue();

            packFileNodeBiConsumer.accept(packFile, ast);
        }
    }

    @NotNull
    public String packName(@NotNull final File file) throws WorkspaceException {
        final String workspacePath = directory.getAbsolutePath();
        final String packPath = file.getAbsolutePath();

        if (!packPath.startsWith(workspacePath))
            throw new WorkspaceException("Cannot read pack name of pack file outside of workspace");

        return packPath.substring(workspacePath.length() + 1).replace("/", ".");
    }

    private void handleWorkspaceError(@NotNull final PackFile pack, @NotNull final WorkspaceException e) {
        logger.log(e.getMessage(), LoggingLevel.WORKSPACE_ERROR);
        Arrays.stream(e.traceBackTokens()).forEach(t -> logger.log(PositionInformationMessage.Builder
                .createBuilder(LoggingLevel.HELP)
                .positionInformation(t, packContentMap.get(pack))
                .build()));
    }

    private void parsePackFile(@NotNull final File file,
                               @NotNull final Map<PackFile, Node> packNodeMap) throws IOException, WorkspaceException {
        parsePackFile(packName(file), file, packNodeMap);
    }

    private void parsePackFile(@NotNull final String name, @NotNull final File file,
                               @NotNull final Map<PackFile, Node> packNodeMap) throws IOException, WorkspaceException {
        final PackFile pack = workspace.createPackage(name);
        try {
            final List<String> content = new ArrayList<>();

            final Node packAST = parser.parse(file, content)
                    .orElseThrow(() -> new WorkspaceException("Could not parse pack file '" + name + "'"));

            packNodeMap.put(pack, packAST);
            packContentMap.put(pack, content);
        } catch (final WorkspaceException e) {
            handleWorkspaceError(pack, e);
            throw new WorkspaceException(e);
        }
    }

    @NotNull
    public ExpressionParser parser() {
        return parser;
    }

}
