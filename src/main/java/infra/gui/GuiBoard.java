package infra.gui;

import domain.model.Display;
import javafx.scene.Group;
import javafx.scene.paint.Color;

final class GuiBoard extends Group {

    private Display currentDisplay;

    GuiBoard() {
        this.currentDisplay = null;
    }

    public void setGame(final Display display) {
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
        this.currentDisplay.getAllCells()
            .stream()
            .forEachOrdered(cell -> {
                final Display.Kind kind = this.currentDisplay.getKind(cell);
                final GuiCell cellDisplay = new GuiCell(
                    toColor(kind),
                    this.currentDisplay.getName(cell),
                    cellSizePx
                );
                cellDisplay.setLayoutY(cell.row() * cellSizePx);
                cellDisplay.setLayoutX(cell.col() * cellSizePx);
                this.getChildren().add(cellDisplay);
            });
    }

    private static Color toColor(final Display.Kind kind) {
        return switch (kind) {
            case EMPTY -> GuiPalette.EMPTY;
            case CHECKPOINT -> GuiPalette.CHECKPOINT;
            case SYSTEM_WALL -> GuiPalette.SYSTEM_WALL;
            case PLAYER_WALL -> GuiPalette.PLAYER_WALL;
        };
    }
}
