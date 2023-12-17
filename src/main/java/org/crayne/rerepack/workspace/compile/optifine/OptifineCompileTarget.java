package org.crayne.rerepack.workspace.compile.optifine;

import org.apache.commons.io.FileUtils;
import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.util.logging.LoggingLevel;
import org.crayne.rerepack.util.logging.message.TraceBackMessage;
import org.crayne.rerepack.workspace.Workspace;
import org.crayne.rerepack.workspace.compile.CompileTarget;
import org.crayne.rerepack.workspace.compile.optifine.resource.cit.CITResource;
import org.crayne.rerepack.workspace.compile.optifine.resource.font.FontResource;
import org.crayne.rerepack.workspace.compile.optifine.resource.lang.LangResource;
import org.crayne.rerepack.workspace.compile.optifine.resource.lang.SpaceFontLangResource;
import org.crayne.rerepack.workspace.compile.optifine.resource.font.SpaceFontResource;
import org.crayne.rerepack.workspace.compile.optifine.util.ImageSplit;
import org.crayne.rerepack.workspace.pack.PackFile;
import org.crayne.rerepack.workspace.pack.character.CharacterContainer;
import org.crayne.rerepack.workspace.pack.character.CharacterStatement;
import org.crayne.rerepack.workspace.pack.lang.LangStatement;
import org.crayne.rerepack.workspace.pack.match.MatchReplaceContainer;
import org.crayne.rerepack.workspace.pack.match.MatchReplaceStatement;
import org.crayne.rerepack.workspace.pack.write.WriteContainer;
import org.crayne.rerepack.workspace.pack.write.WriteStatement;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static org.crayne.rerepack.workspace.compile.optifine.resource.Resource.fileNameOfPath;
import static org.crayne.rerepack.workspace.compile.optifine.resource.Resource.withDifferentFileExtension;

public class OptifineCompileTarget implements CompileTarget {

    @NotNull
    private final File outputDirectory;

    public OptifineCompileTarget(@NotNull final File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @NotNull
    public File outputDirectory() {
        return outputDirectory;
    }

    public void compile(@NotNull final Workspace workspace) {
        if (deleteOldOutput(workspace)) return;
        compileAllMatchReplacements(workspace);
        compileAllFileWrites(workspace);
        compileAllCharacterReplacements(workspace);
        compileSpaceFont(workspace);
        compileLangStatements(workspace);
    }

    private static void createParentDirectories(@NotNull final File file) throws IOException {
        final File parentDirectory = file.getParentFile();
        if (!parentDirectory.isDirectory() && !parentDirectory.mkdirs())
            throw new IOException("Could not create parent directory of " + file.getPath());
    }

    public boolean deleteOldOutput(@NotNull final Workspace workspace) {
        try {
            if (outputDirectory.exists()) FileUtils.deleteDirectory(outputDirectory);
        } catch (final IOException e) {
            workspace.logger().log("Could not delete old output directory: "
                    + e.getMessage(), LoggingLevel.PACKING_ERROR);
            return true;
        }
        return false;
    }

    public void compileLangStatements(@NotNull final Workspace workspace) {
        final Map<LangResource, Set<LangStatement>> langResourceSetMap = new HashMap<>();
        workspace.langContainer().langStatements().forEach(langStatement -> {
            final Set<LangResource> langResources = LangResource.createLangResources(langStatement);
            langResources.forEach(langResource -> {
                langResourceSetMap.putIfAbsent(langResource, new HashSet<>());
                langResourceSetMap.get(langResource).add(langStatement);
            });
        });

        langResourceSetMap.forEach((langResource, langStatements) ->
                langStatements.forEach(langResource::addLangStatementElements));

        for (final LangResource langResource : langResourceSetMap.keySet())
            compileLanguageJson(workspace, langResource);
    }

    public void compileFileCopy(@NotNull final Workspace workspace, @NotNull final String source,
                                @NotNull final String destination) {
        final File sourceFile = new File(workspace.directory(), source);
        final File destinationFile = new File(outputDirectory, destination);

        if (!sourceFile.isFile()) {
            workspace.logger().log("Cannot find repack texture file '"
                    + sourceFile.getPath() + "', could not copy to output resource pack", LoggingLevel.PACKING_ERROR);
            return;
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            destinationFile.getParentFile().mkdirs();
            FileUtils.copyFile(sourceFile, destinationFile);
        } catch (final IOException e) {
            workspace.logger().log("Could not copy file '" + sourceFile.getAbsolutePath()
                    + "' to '" + destinationFile.getAbsolutePath()
                    + "': " + e.getMessage(), LoggingLevel.PACKING_ERROR);
        }
    }

    public void compileAllFileWrites(@NotNull final Workspace workspace) {
        final Set<String> alreadyWrittenTo = new HashSet<>();
        final Set<WriteContainer> writeContainers = workspace.packFiles()
                .stream()
                .map(PackFile::writeContainer)
                .collect(Collectors.toSet());

        writeContainers.stream()
                .map(WriteContainer::writeStatements)
                .flatMap(Collection::stream)
                .forEach(w -> compileWriteStatement(workspace, w, alreadyWrittenTo));

        writeContainers.stream()
                .map(WriteContainer::copyStatementsFull)
                .flatMap(Collection::stream)
                .forEach(w -> compileWriteStatement(workspace, w, alreadyWrittenTo));

        writeContainers.stream()
                .map(WriteContainer::copyStatementsRaw)
                .flatMap(Collection::stream)
                .forEach(w -> compileFileCopy(workspace,
                        w.initializedSourcePath().orElseThrow().token(),
                        w.initializedDestinationPath().orElseThrow().token()));
    }

    public void compileWriteStatement(@NotNull final Workspace workspace,
                                      @NotNull final WriteStatement writeStatement,
                                      @NotNull final Set<String> alreadyWrittenTo) {
        final Token destinationPathToken = writeStatement.initializedDestinationPath().orElseThrow();
        final String destinationPath = destinationPathToken.token();

        if (alreadyWrittenTo.contains(destinationPath))
            workspace.logger().log(TraceBackMessage.Builder
                    .createBuilder("Overwriting previously created file " +
                            "'" + destinationPath + "'", LoggingLevel.WARN)
                    .at(destinationPathToken)
                    .build());

        try {
            final File file = new File(outputDirectory, destinationPath);
            createParentDirectories(file);
            Files.write(file.toPath(), writeStatement.lines()
                    .stream()
                    .map(Token::token)
                    .toList());
        } catch (final IOException e) {
            workspace.logger().log(TraceBackMessage.Builder
                    .createBuilder("Could not write to file '" + destinationPath + "': "
                            + e.getMessage(), LoggingLevel.PACKING_ERROR)
                    .at(destinationPathToken)
                    .build());
        }
    }

    public void compileAllCharacterReplacements(@NotNull final Workspace workspace) {
        final FontResource fontResource = new FontResource();

        workspace.packFiles()
                .stream()
                .map(PackFile::characterContainer)
                .map(CharacterContainer::characterStatements)
                .flatMap(Collection::stream)
                .forEach(c -> compileCharacterStatement(workspace, c, fontResource));

        fontResource.addSpaceFontCharacters();

        try {
            final File defaultJson = new File(outputDirectory, FontResource.DEFAULT_FONT_JSON);
            createParentDirectories(defaultJson);

            Files.writeString(defaultJson.toPath(), fontResource.encode(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            workspace.logger().log("Could not create default.json file: " + e.getMessage(),
                    LoggingLevel.PACKING_ERROR);
            e.printStackTrace();
        }
    }

    public void compileSpaceFont(@NotNull final Workspace workspace) {
        compileSpaceFontDefaultJson(workspace);
        compileSpaceFontLangJson(workspace);
        compileSpaceFontSplitter(workspace);
    }

    public void compileSpaceFontSplitter(@NotNull final Workspace workspace) {
        final BufferedImage splitterImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        ImageSplit.drawInvisibleCorner(splitterImage, 255, 255);

        try {
            final File splitterImageFile = new File(outputDirectory, SpaceFontResource.SPACE_FONT_SPLITTER);
            createParentDirectories(splitterImageFile);

            ImageIO.write(splitterImage, "png", splitterImageFile);
        } catch (final IOException e) {
            workspace.logger().log("Could not create space font splitter.png texture file: "
                            + e.getMessage(), LoggingLevel.PACKING_ERROR);
            e.printStackTrace();
        }
    }

    public void compileLanguageJson(@NotNull final Workspace workspace, @NotNull final LangResource langResource) {
        try {
            final File langJson = new File(outputDirectory, langResource.languageName());
            createParentDirectories(langJson);

            Files.writeString(langJson.toPath(), langResource.encode(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            workspace.logger().log("Could not create language file" + langResource.languageName() + ": " + e.getMessage(),
                    LoggingLevel.PACKING_ERROR);
            e.printStackTrace();
        }
    }

    public void compileSpaceFontLangJson(@NotNull final Workspace workspace) {
        final SpaceFontLangResource spaceFontLangResource = new SpaceFontLangResource();
        spaceFontLangResource.addAllSpaces();

        compileLanguageJson(workspace, spaceFontLangResource);
    }

    public void compileSpaceFontDefaultJson(@NotNull final Workspace workspace) {
        try {
            final SpaceFontResource spaceFontResource = new SpaceFontResource();
            spaceFontResource.addSpaceFontCharacters();

            final File defaultJson = new File(outputDirectory, SpaceFontResource.SPACE_DEFAULT_FONT_JSON);
            createParentDirectories(defaultJson);

            Files.writeString(defaultJson.toPath(), spaceFontResource.encode(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            workspace.logger().log("Could not create space font default.json file: " + e.getMessage(),
                    LoggingLevel.PACKING_ERROR);
            e.printStackTrace();
        }
    }

    public void compileCharacterStatement(@NotNull final Workspace workspace,
                                          @NotNull final CharacterStatement characterStatement,
                                          @NotNull final FontResource fontResource) {
        final Token filepath = characterStatement.bitmapFilePath();

        final File copyFrom = new File(workspace.directory(), filepath.token());
        final BufferedImage originalImage;

        try {
            originalImage = ImageIO.read(copyFrom);
        } catch (final IOException e) {
            workspace.logger().log(TraceBackMessage.Builder
                    .createBuilder("Could not read texture file '" + filepath + "' as an image: "
                            + e.getMessage(), LoggingLevel.PACKING_ERROR)
                    .at(filepath)
                    .build());
            return;
        }
        final double resolution = characterStatement.resolution();
        final int leftXOffset = 100;
        final int scaledSplitSize = (int) (256 / resolution);
        final StringBuilder text = new StringBuilder(FontResource.createFullWidthSpace(-leftXOffset - 8));
        final List<ImageSplit> splitTextures = ImageSplit.splitImage(originalImage, 256, resolution,
                characterStatement.characterList().get(0).charAt(0), text);

        if (splitTextures.size() > 1)
            workspace.logger().info("Character texture " + copyFrom.getName() + " was split into "
                    + splitTextures.size() + " textures due to exceeding the maximum size. " +
                    "Full string to use ingame for display: " + text);

        try {
            for (final ImageSplit imageSplit : splitTextures) {
                final String suffix = splitTextures.size() == 1
                        ? ".png"
                        : "_" + imageSplit.row() + "_" + imageSplit.column() + ".png";

                final String splitFilePath = withDifferentFileExtension(fileNameOfPath(filepath.token()), suffix);

                final File destinationImage = new File(outputDirectory,
                        "assets/minecraft/textures/" + splitFilePath);

                createParentDirectories(destinationImage);
                ImageIO.write(imageSplit.splitImage(), "png", destinationImage);

                fontResource.addCharacterElement(characterStatement.createSplit(imageSplit,
                        splitFilePath, originalImage.getHeight(), scaledSplitSize));
            }
        } catch (final IOException e) {
            workspace.logger().log(TraceBackMessage.Builder
                    .createBuilder("Could not copy file '" + filepath + "' to output: "
                            + e.getMessage(), LoggingLevel.PACKING_ERROR)
                    .at(filepath)
                    .build());
        }
    }

    public void compileMatchReplace(@NotNull final MatchReplaceContainer matchReplaceContainer,
                                    @NotNull final Map<CITResource, Set<Token>> citResourceSetMap) {
        matchReplaceContainer.matchesReplacements().forEach(matchReplaceStatement -> matchReplaceStatement
                .replacements()
                .forEach(replacement -> {
                    final CITResource resource = new CITResource(matchReplaceStatement.matches(), replacement.value());
                    citResourceSetMap.putIfAbsent(resource, new HashSet<>());
                    citResourceSetMap.get(resource).add(replacement.key());
                }));
    }

    public void compileAllMatchReplacements(@NotNull final Workspace workspace) {
        final Map<CITResource, Set<Token>> citResourceSetMap = new HashMap<>();
        workspace.packFiles()
                .stream()
                .map(PackFile::matchReplaceContainer)
                .forEach(matchReplaceContainer -> compileMatchReplace(matchReplaceContainer, citResourceSetMap));

        citResourceSetMap.forEach((citResource, items) -> {
            final Set<String> itemMatches = new HashSet<>();
            items.forEach(item -> {
                final Set<String> matched = MatchReplaceStatement.parseItems(item);
                if (matched.isEmpty()) workspace.logger().log(TraceBackMessage.Builder
                        .createBuilder("No items were found for item match '" + item + "'", LoggingLevel.WARN)
                        .at(item)
                        .build());

                itemMatches.addAll(matched);
            });
            citResource.valueMap().put("items", String.join(" ", itemMatches));
            citResource.valueMap().put("type", "item");
        });

        citResourceSetMap.keySet().forEach(r -> {
            compileFileCopy(workspace, r.texturePath(), r.citFilePath());
            try {
                final String propertiesFileName = withDifferentFileExtension(r.citFilePath(),
                        ".properties");
                final File propertiesFile = new File(outputDirectory, propertiesFileName);

                Files.writeString(propertiesFile.toPath(), r.encode(), StandardCharsets.UTF_8);
            } catch (final IOException e) {
                workspace.logger().log("Could not create properties file for" +
                        " cit with texture " + r.textureName() + ": " + e.getMessage(), LoggingLevel.PACKING_ERROR);
            }
        });

    }

}
