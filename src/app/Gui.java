package app;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import think.representation.Board;
import think.representation.Cell;

public class Gui {

    public static final double CELL_SIZE = 24.0; // Pixels.
}

class BoardDisplay extends Group {

    public BoardDisplay(Board board) {
        for (int i = 0; i < board.getBoundI(); i++) {
            for (int j = 0; j < board.getBoundJ(); j++) {
                CellDisplay cell = new CellDisplay(board.getCell(i, j));
                cell.setLayoutX(j * Gui.CELL_SIZE);
                cell.setLayoutY(i * Gui.CELL_SIZE);
                getChildren().add(cell);
            }
        }
    }
}

class CellDisplay extends Group {

    public CellDisplay(Cell cell) {
        Rectangle rect = new Rectangle(Gui.CELL_SIZE, Gui.CELL_SIZE);
        rect.setFill(getColor(cell));
        rect.setStroke(Color.DARKGRAY);

        if (
            cell.type() == Cell.CellType.CHECKPOINT ||
            cell.type() == Cell.CellType.TELEPORT_IN ||
            cell.type() == Cell.CellType.TELEPORT_OUT
        ) {
            Text label = new Text(String.valueOf(cell.association()));
            label.setFont(Font.font(Gui.CELL_SIZE * 0.5));
            label.setX((Gui.CELL_SIZE - label.getLayoutBounds().getWidth()) * 0.5);
            label.setY((Gui.CELL_SIZE + label.getLayoutBounds().getHeight()) * 0.5);
            getChildren().addAll(rect, label);
        } else {
            getChildren().add(rect);
        }
    }

    private Color getColor(Cell cell) {
        // Loosely based on Pathery default colors.
        return switch (cell.type()) {
            case BRICK -> Color.CORAL;
            case CHECKPOINT -> Color.PALEGOLDENROD;
            case FINISH -> Color.BURLYWOOD;
            case NOTHING -> Color.WHITESMOKE;
            case RUBBER -> Color.SLATEGRAY;
            case START -> Color.FORESTGREEN;
            case TELEPORT_IN -> Color.CADETBLUE;
            case TELEPORT_OUT -> Color.CORNFLOWERBLUE;
        };
    }
}
