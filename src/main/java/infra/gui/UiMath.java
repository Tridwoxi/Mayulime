package infra.gui;

import java.io.File;
import java.util.Locale;
import javafx.scene.paint.Color;
import think.domain.model.Tile;

final class UiMath {

    private UiMath() {}

    static double clampCellSize(final double rawSize) {
        return Math.max(Gui.MIN_CELL_SIZE_PX, Math.min(Gui.MAX_CELL_SIZE_PX, rawSize));
    }

    static boolean isSupportedMapFile(final File file) {
        final String name = file.getName().toLowerCase(Locale.US);
        return name.endsWith(".mapcode");
    }

    static String walls(final int spentWalls, final int wallBudget) {
        return String.format(Locale.US, "%d/%d", spentWalls, wallBudget);
    }

    static int countPlayerWalls(final Submission display) {
        if (display == null) {
            return 0;
        }
        int count = 0;
        for (int row = 0; row < display.getNumRows(); row += 1) {
            for (int col = 0; col < display.getNumCols(); col += 1) {
                if (display.getTile(row, col) == Tile.PLAYER_WALL) {
                    count += 1;
                }
            }
        }
        return count;
    }

    static String maze(final int numRows, final int numCols) {
        if (numRows <= 0 || numCols <= 0) {
            return "-";
        }
        return String.format(Locale.US, "%d x %d", numRows, numCols);
    }

    static String elapsed(final long startNanos, final long nowNanos) {
        if (startNanos <= 0L || nowNanos < startNanos) {
            return "-";
        }
        final long elapsedSeconds = (nowNanos - startNanos) / 1_000_000_000L;
        final long hours = elapsedSeconds / 3600L;
        final long minutes = (elapsedSeconds % 3600L) / 60L;
        final long seconds = elapsedSeconds % 60L;
        if (hours > 0L) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    static String cellLabel(final String content, final double sizePx) {
        if (sizePx < 10.0 || content.isBlank()) {
            return "";
        }
        if (sizePx < 15.0 && content.startsWith("c") && content.length() > 1) {
            return content.substring(1);
        }
        return content;
    }

    static Color cellLabelColor(final Color background, final UiPalette palette) {
        final Color darkLabel = palette.background();
        final Color lightLabel = palette.foreground();
        if (contrastRatio(background, darkLabel) >= contrastRatio(background, lightLabel)) {
            return darkLabel;
        }
        return lightLabel;
    }

    private static double contrastRatio(final Color left, final Color right) {
        final double leftLuminance = relativeLuminance(left);
        final double rightLuminance = relativeLuminance(right);
        final double lighter = Math.max(leftLuminance, rightLuminance);
        final double darker = Math.min(leftLuminance, rightLuminance);
        return (lighter + 0.05) / (darker + 0.05);
    }

    private static double relativeLuminance(final Color color) {
        return (
            0.2126 * channelLuminance(color.getRed()) +
            0.7152 * channelLuminance(color.getGreen()) +
            0.0722 * channelLuminance(color.getBlue())
        );
    }

    private static double channelLuminance(final double channel) {
        if (channel <= 0.03928) {
            return channel / 12.92;
        }
        return Math.pow((channel + 0.055) / 1.055, 2.4);
    }
}
