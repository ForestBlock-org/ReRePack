package org.crayne.rerepack.workspace.compile.optifine.util;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class ImageSplit {

    private final int row, column, splitIndex;

    @NotNull
    private final BufferedImage splitImage;

    public ImageSplit(final int row, final int column, final int splitIndex, final int splitSize, @NotNull final BufferedImage sourceImage) {
        this.row = row;
        this.column = column;
        this.splitIndex = splitIndex;
        this.splitImage = new BufferedImage(splitSize, splitSize, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D graphics2D = splitImage.createGraphics();
        final int startX = splitSize * column;
        final int startY = splitSize * row;

        final int endX = Math.min(startX + splitSize, sourceImage.getWidth());
        final int endY = Math.min(startY + splitSize, sourceImage.getHeight());

        graphics2D.drawImage(sourceImage, 0, 0, endX - startX, endY - startY, startX, startY, endX, endY, null);
        graphics2D.dispose();

        drawInvisibleCorner(splitImage, splitSize - 1, splitSize - 1);
    }

    @NotNull
    public static List<ImageSplit> splitImage(@NotNull final BufferedImage sourceImage,
                                              final int splitSize, final double resolution) {

        final double scale = 1.0d / resolution;
        final int height = sourceImage.getHeight(), width = sourceImage.getWidth();

        final int splitRows = (height - 1) / splitSize + 1;
        final int splitCols = (width - 1) / splitSize + 1;

        final ImageSplit[] splits = new ImageSplit[splitRows * splitCols];
        final int scaledSplitSize = (int) (splitSize * scale);

        int i = 0;
        for (int row = 0; row < splitRows; row++) {
            for (int col = 0; col < splitCols; col++) {
                final boolean last = i == splits.length - 1;
                splits[i] = new ImageSplit(row, col, i, splitSize, sourceImage);
                i++;
            }
        }
        return Arrays.stream(splits).toList();
    }

    public static void drawInvisibleCorner(@NotNull final BufferedImage image, final int x, final int y) {
        if (image.getRGB(x, y) == 0)
            image.setRGB(x, y, new Color(0, 0, 0, 1).getRGB());
    }

    @NotNull
    public BufferedImage splitImage() {
        return splitImage;
    }

    public int column() {
        return column;
    }

    public int row() {
        return row;
    }

    public int splitIndex() {
        return splitIndex;
    }

}