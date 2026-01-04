package app;

import java.io.File;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import think.representation.Board;
import think.representation.Cell;

class Gui extends VBox {

    private static final double CELL_SIZE = 20.0; // Pixels.
    private static final double PADDING = 50.0;
    private static final double SPACING = 50.0;

    private BoardDisplay boardDisplay;
    private final Text scoreDisplay;
    private final Text rubberDisplay;

    public Gui() {
        super(SPACING);
        this.boardDisplay = new BoardDisplay();
        this.scoreDisplay = new Text();
        this.rubberDisplay = new Text();
        setPadding(new Insets(PADDING));
        getChildren().addAll(
            boardDisplay, // Must be at position 0 to match with setBoard(Board).
            new HBox(SPACING, scoreDisplay, rubberDisplay, makeButton())
        );
    }

    public void setBoard(Board board) {
        boardDisplay = new BoardDisplay(board, CELL_SIZE);
        getChildren().set(0, boardDisplay);
    }

    private Button makeButton() {
        Button upload = new Button("Upload problem");
        upload.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser
                .getExtensionFilters()
                .add(new ExtensionFilter("Pathery level specification", "*.txt"));
            Window active = getScene() == null ? null : getScene().getWindow();
            File chosen = chooser.showOpenDialog(active);
            if (chosen != null) {
                Main.solveProblem(chosen);
            }
        });
        return upload;
    }
}

class BoardDisplay extends Group {

    public BoardDisplay() {}

    public BoardDisplay(Board board, double cellSize) {
        for (int i = 0; i < board.getBoundI(); i++) {
            for (int j = 0; j < board.getBoundJ(); j++) {
                CellDisplay cell = new CellDisplay(board.getCell(i, j), cellSize);
                cell.setLayoutX(j * cellSize);
                cell.setLayoutY(i * cellSize);
                getChildren().add(cell);
            }
        }
    }
}

class CellDisplay extends Group {

    public CellDisplay(Cell cell, double cellSize) {
        Rectangle rect = new Rectangle(cellSize, cellSize);
        rect.setFill(getColor(cell));
        rect.setStroke(Color.DARKGRAY);

        if (
            cell.type() == Cell.CellType.CHECKPOINT ||
            cell.type() == Cell.CellType.TELEPORT_IN ||
            cell.type() == Cell.CellType.TELEPORT_OUT
        ) {
            Text label = new Text(String.valueOf(cell.association()));
            label.setFont(Font.font(cellSize * 0.5));
            label.setX((cellSize - label.getLayoutBounds().getWidth()) * 0.5);
            label.setY((cellSize + label.getLayoutBounds().getHeight()) * 0.5);
            getChildren().addAll(rect, label);
        } else {
            getChildren().add(rect);
        }
    }

    private Color getColor(Cell cell) {
        return switch (cell.type()) {
            case BRICK -> Color.DARKORANGE;
            case CHECKPOINT -> Color.FIREBRICK;
            case NOTHING -> Color.WHEAT;
            case RUBBER -> Color.DARKSLATEGRAY;
            case TELEPORT_IN -> Color.DARKBLUE;
            case TELEPORT_OUT -> Color.FORESTGREEN;
        };
    }
}
