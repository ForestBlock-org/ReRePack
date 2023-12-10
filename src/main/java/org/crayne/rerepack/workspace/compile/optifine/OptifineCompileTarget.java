package org.crayne.rerepack.workspace.compile.optifine;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.util.logging.LoggingLevel;
import org.crayne.rerepack.util.logging.message.TraceBackMessage;
import org.crayne.rerepack.util.minecraft.VanillaItem;
import org.crayne.rerepack.workspace.Workspace;
import org.crayne.rerepack.workspace.compile.CompileTarget;
import org.crayne.rerepack.workspace.compile.optifine.resource.Resource;
import org.crayne.rerepack.workspace.compile.optifine.resource.cit.CITResource;
import org.crayne.rerepack.workspace.compile.optifine.resource.font.FontResource;
import org.crayne.rerepack.workspace.compile.optifine.resource.font.space.SpaceFontLangResource;
import org.crayne.rerepack.workspace.compile.optifine.resource.font.space.SpaceFontResource;
import org.crayne.rerepack.workspace.pack.PackFile;
import org.crayne.rerepack.workspace.pack.character.CharacterContainer;
import org.crayne.rerepack.workspace.pack.character.CharacterStatement;
import org.crayne.rerepack.workspace.pack.match.MatchReplaceContainer;
import org.crayne.rerepack.workspace.pack.write.WriteContainer;
import org.crayne.rerepack.workspace.pack.write.WriteStatement;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

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
        workspace.packFiles()
                .stream()
                .map(PackFile::writeContainer)
                .map(WriteContainer::writeStatements)
                .flatMap(Collection::stream)
                .forEach(w -> compileWriteStatement(workspace, w, alreadyWrittenTo));
    }

    public void compileWriteStatement(@NotNull final Workspace workspace,
                                      @NotNull final WriteStatement writeStatement,
                                      @NotNull final Set<String> alreadyWrittenTo) {
        final Token destinationPathToken = writeStatement.destinationPath();
        final String destinationPath = destinationPathToken.token();

        if (alreadyWrittenTo.contains(destinationPath))
            workspace.logger().log(TraceBackMessage.Builder
                    .createBuilder("Overwriting previously created file " +
                            "'" + destinationPath + "'", LoggingLevel.WARN)
                    .at(destinationPathToken)
                    .build());

        try {
            Files.write(new File(outputDirectory, destinationPath).toPath(), writeStatement.lines()
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
        drawInvisibleCorner(splitterImage, 255, 255);

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

    @SuppressWarnings("SameParameterValue")
    private static void drawInvisibleCorner(@NotNull final BufferedImage bufferedImage, final int x, final int y) {
        if (bufferedImage.getRGB(x, y) == 0)
            bufferedImage.setRGB(x, y, new Color(0, 0, 0, 1).getRGB());
    }

    public void compileSpaceFontLangJson(@NotNull final Workspace workspace) {
        try {
            final SpaceFontLangResource spaceFontLangResource = new SpaceFontLangResource();
            spaceFontLangResource.addAllSpaces();

            final File langJson = new File(outputDirectory, SpaceFontLangResource.LANG_JSON_FILE);
            createParentDirectories(langJson);

            Files.writeString(langJson.toPath(), spaceFontLangResource.encode(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            workspace.logger().log("Could not create space font en_us.json file: " + e.getMessage(),
                    LoggingLevel.PACKING_ERROR);
            e.printStackTrace();
        }
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
        fontResource.addCharacterElement(characterStatement);

        final Token filepath = characterStatement.bitmapFilePath();
        final File copyTo = new File(outputDirectory, "assets/minecraft/textures/"
                + Resource.fileNameOfPath(filepath.token()));

        final File copyFrom = new File(workspace.directory(), filepath.token());

        try {
            createParentDirectories(copyTo);
            Files.copy(copyFrom.toPath(), copyTo.toPath());
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
                final Set<String> matched = VanillaItem.allMatching(item.token());
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
                final String propertiesFileName = StringUtils.substringBefore(r.citFilePath(),
                        ".") + ".properties";

                final File propertiesFile = new File(outputDirectory, propertiesFileName);

                Files.writeString(propertiesFile.toPath(), r.encode(), StandardCharsets.UTF_8);
            } catch (final IOException e) {
                workspace.logger().log("Could not create properties file for" +
                        " cit with texture " + r.textureName() + ": " + e.getMessage(), LoggingLevel.PACKING_ERROR);
            }
        });

    }

}
