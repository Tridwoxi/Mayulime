package infra.gui;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import think.domain.model.Feature;
import think.manager.StatusUpdate;

final class BoardView extends Canvas {

    private UiPalette palette;
    private StatusUpdate display;
    private double cellSizePx;

    BoardView(final UiPalette initialPalette) {
        this.palette = initialPalette;
        this.display = null;
        this.cellSizePx = 0.0;
    }

    public void render(final StatusUpdate display, final double cellSizePx) {
        this.display = display;
        this.cellSizePx = cellSizePx;
        if (display == null) {
            this.setWidth(1.0);
            this.setHeight(1.0);
            return;
        }
        final int numRows = display.getNumRows();
        final int numCols = display.getNumCols();
        this.setWidth(numCols * cellSizePx);
        this.setHeight(numRows * cellSizePx);

        final GraphicsContext graphics = this.getGraphicsContext2D();
        graphics.clearRect(0.0, 0.0, this.getWidth(), this.getHeight());
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.CENTER);
        graphics.setFont(Font.font(Gui.FONT_NAME, Math.max(7.0, cellSizePx * 0.32)));
        graphics.setLineWidth(Math.max(0.7, cellSizePx * 0.04));
        graphics.setStroke(this.palette.outline());

        for (int row = 0; row < display.getNumRows(); row += 1) {
            for (int col = 0; col < display.getNumCols(); col += 1) {
                final Feature feature = display.getFeature(row, col);
                final double x = col * cellSizePx;
                final double y = row * cellSizePx;
                final Color fill = this.toColor(feature);
                graphics.setFill(fill);
                graphics.fillRect(x, y, cellSizePx, cellSizePx);
                graphics.strokeRect(x, y, cellSizePx, cellSizePx);

                final String labelText = UiMath.cellLabel(
                    cellLabel(display, row, col, feature),
                    cellSizePx
                );
                if (!labelText.isBlank()) {
                    graphics.setFill(UiMath.cellLabelColor(fill, this.palette));
                    graphics.fillText(labelText, x + cellSizePx * 0.5, y + cellSizePx * 0.5);
                }
            }
        }
    }

    public void clear() {
        this.display = null;
        this.cellSizePx = 0.0;
        this.getGraphicsContext2D().clearRect(0.0, 0.0, this.getWidth(), this.getHeight());
        this.setWidth(1.0);
        this.setHeight(1.0);
    }

    public void applyPalette(final UiPalette paletteToApply) {
        this.palette = paletteToApply;
        if (this.display != null && this.cellSizePx > 0.0) {
            this.render(this.display, this.cellSizePx);
        }
    }

    private Color toColor(final Feature feature) {
        return switch (feature) {
            case BLANK -> this.palette.empty();
            case CHECKPOINT -> this.palette.checkpoint();
            case SYSTEM_WALL -> this.palette.systemWall();
            case PLAYER_WALL -> this.palette.playerWall();
        };
    }

    private static String cellLabel(
        final StatusUpdate display,
        final int row,
        final int col,
        final Feature feature
    ) {
        if (feature != Feature.CHECKPOINT) {
            return "";
        }
        final int checkpointOrder = display.getCheckpointOrder(row, col);
        if (checkpointOrder < 0) {
            return "c";
        }
        return "c" + checkpointOrder;
    }
}
