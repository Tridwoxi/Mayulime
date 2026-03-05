package infra.gui;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import think.domain.model.Feature;
import think.manager.StatusUpdate;

final class BoardView extends Group {

    BoardView() {}

    public void render(final StatusUpdate display, final double cellSizePx) {
        this.getChildren().clear();
        if (display == null) {
            return;
        }
        for (int row = 0; row < display.getNumRows(); row += 1) {
            for (int col = 0; col < display.getNumCols(); col += 1) {
                final Feature feature = display.getFeature(row, col);
                final BoardCellView cell = new BoardCellView(
                    toColor(feature),
                    cellLabel(display, row, col, feature),
                    cellSizePx
                );
                cell.setLayoutY(row * cellSizePx);
                cell.setLayoutX(col * cellSizePx);
                this.getChildren().add(cell);
            }
        }
    }

    public void clear() {
        this.getChildren().clear();
    }

    private static Color toColor(final Feature feature) {
        return switch (feature) {
            case BLANK -> UiPalette.EMPTY;
            case CHECKPOINT -> UiPalette.CHECKPOINT;
            case SYSTEM_WALL -> UiPalette.SYSTEM_WALL;
            case PLAYER_WALL -> UiPalette.PLAYER_WALL;
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
