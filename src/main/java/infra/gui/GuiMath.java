package infra.gui;

import java.io.File;
import java.util.Locale;
import javafx.scene.paint.Color;

final class GuiMath {

    static final double DEFAULT_ZOOM = 1.0;

    private static final double MIN_ZOOM = 0.4;
    private static final double MAX_ZOOM = 3.0;
    private static final double ZOOM_STEP = 1.15;

    private GuiMath() {}

    static double clampZoom(final double requestedZoom) {
        return Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, requestedZoom));
    }

    static double zoomIn(final double currentZoom) {
        return clampZoom(currentZoom * ZOOM_STEP);
    }

    static double zoomOut(final double currentZoom) {
        return clampZoom(currentZoom / ZOOM_STEP);
    }

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

    static String grid(final int numRows, final int numCols) {
        if (numRows <= 0 || numCols <= 0) {
            return "-";
        }
        return String.format(Locale.US, "%d x %d", numRows, numCols);
    }

    static String zoomPercent(final double zoomMultiplier) {
        final double bounded = Math.max(0.0, zoomMultiplier);
        final int percent = (int) Math.round(bounded * 100.0);
        return percent + "%";
    }

    static String elapsed(final long startNanos, final long nowNanos) {
        if (startNanos <= 0L || nowNanos <= startNanos) {
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

    static Color cellLabelColor(final Color background) {
        if (background.getBrightness() > 0.6) {
            return GuiPalette.BACKGROUND;
        }
        return GuiPalette.FOREGROUND;
    }
}
