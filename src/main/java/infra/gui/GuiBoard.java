package infra.gui;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import think.domain.model.Feature;
import think.manager.StatusUpdate;

final class GuiBoard extends Group {

    private StatusUpdate currentDisplay;

    GuiBoard() {
        this.currentDisplay = null;
    }

    public void setGame(final StatusUpdate display) {
        this.currentDisplay = display;
    }

    public void clear() {
        this.currentDisplay = null;
        this.getChildren().clear();
    }

    public void rerenderIfPresent(final double cellSizePx) {
        if (this.currentDisplay == null) {
            return;
        }

        this.getChildren().clear();
        for (int row = 0; row < this.currentDisplay.getNumRows(); row += 1) {
            for (int col = 0; col < this.currentDisplay.getNumCols(); col += 1) {
                final Feature feature = this.currentDisplay.getFeature(row, col);
                final GuiCell cellDisplay = new GuiCell(
                    toColor(feature),
                    cellLabel(feature),
                    cellSizePx
                );
                cellDisplay.setLayoutY(row * cellSizePx);
                cellDisplay.setLayoutX(col * cellSizePx);
                this.getChildren().add(cellDisplay);
            }
        }
    }

    private static Color toColor(final Feature feature) {
        return switch (feature) {
            case BLANK -> GuiPalette.EMPTY;
            case CHECKPOINT -> GuiPalette.CHECKPOINT;
            case SYSTEM_WALL -> GuiPalette.SYSTEM_WALL;
            case PLAYER_WALL -> GuiPalette.PLAYER_WALL;
        };
    }

    private static String cellLabel(final Feature feature) {
        return switch (feature) {
            case CHECKPOINT -> "c";
            default -> "";
        };
    }
}
