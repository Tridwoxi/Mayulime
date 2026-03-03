package infra.io;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import think.domain.repr.Display;

final class GameDisplay extends Group {

    private Display currentDisplay;

    GameDisplay() {
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
                final CellDisplay cellDisplay = new CellDisplay(
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
            case EMPTY -> PatheryColors.EMPTY;
            case CHECKPOINT -> PatheryColors.CHECKPOINT;
            case SYSTEM_WALL -> PatheryColors.SYSTEM_WALL;
            case PLAYER_WALL -> PatheryColors.PLAYER_WALL;
        };
    }
}
