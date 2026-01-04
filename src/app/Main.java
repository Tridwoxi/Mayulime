package app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;
import think.Core;
import think.representation.Board;
import think.representation.Cell;
import think.representation.Parser;

public final class Main extends Application {

    private static Gui gui;

    @Override
    public void start(final Stage primaryStage) {
        gui = new Gui();
        Scene mainScene = new Scene(gui);
        mainScene.setFill(Color.GRAY);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Pathery Solver");
        primaryStage.show();
    }

    public static void solveProblem(File file) {
        try {
            new Core(Parser.parse(Files.readString(file.toPath())));
        } catch (IllegalArgumentException e) {
            System.err.println("Bad specification: " + e);
        } catch (IOException e) {
            System.err.println("Can't read file.");
        }
    }

    public static void recieveUpdate(Board board, int score, int remainingRubbers) {
        gui.showUpdate(board, score, remainingRubbers);
    }
}

class Gui extends VBox {

    private static final double CELL_SIZE = 50.0; // Pixels.
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
        scoreDisplay.setFill(PatheryColors.FOREGROUND);
        rubberDisplay.setFill(PatheryColors.FOREGROUND);

        setBackground(Background.fill(PatheryColors.BACKGROUND));
        setPadding(new Insets(PADDING));
        setAlignment(Pos.TOP_CENTER);
        HBox stats = new HBox(SPACING, scoreDisplay, rubberDisplay, makeButton());
        stats.setAlignment(Pos.CENTER);
        getChildren().addAll(boardDisplay, stats);
    }

    public void showUpdate(Board board, int score, int remainingRubber) {
        boardDisplay = new BoardDisplay(board, CELL_SIZE);
        this.scoreDisplay.setText("Score: " + score);
        this.rubberDisplay.setText("Remaining walls: " + remainingRubber);
        getChildren().set(0, boardDisplay); // Must target whatever constructor decided.

        if (
            getScene() != null &&
            getScene().getWindow() != null &&
            getScene().getWindow() instanceof Stage stage
        ) {
            stage.sizeToScene();
        }
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
        rect.setStroke(PatheryColors.BACKGROUND);

        if (
            cell.type() == Cell.CellType.CHECKPOINT ||
            cell.type() == Cell.CellType.TELEPORT_IN ||
            cell.type() == Cell.CellType.TELEPORT_OUT
        ) {
            Text label = new Text(String.valueOf(cell.association()));
            label.setFill(PatheryColors.FOREGROUND);
            label.setFont(Font.font(cellSize * 0.5));
            Bounds bounds = label.getLayoutBounds();
            label.setX((cellSize - bounds.getWidth()) * 0.5 - bounds.getMinX());
            label.setY((cellSize - bounds.getHeight()) * 0.5 - bounds.getMinY());
            getChildren().addAll(rect, label);
        } else {
            getChildren().add(rect);
        }
    }

    private Color getColor(Cell cell) {
        return switch (cell.type()) {
            case BRICK -> PatheryColors.BRICK;
            case CHECKPOINT -> PatheryColors.CHECKPOINT;
            case NOTHING -> PatheryColors.NOTHING;
            case RUBBER -> PatheryColors.RUBBER;
            case TELEPORT_IN -> PatheryColors.TELEPORT;
            case TELEPORT_OUT -> PatheryColors.TELEPORT;
        };
    }
}

class PatheryColors {

    // Approximate average color, picked from Pathery.com
    public static final Color BACKGROUND = Color.web("121212");
    public static final Color BRICK = Color.web("723736");
    public static final Color RUBBER = Color.web("3c3d3c");
    public static final Color NOTHING = Color.web("e2e8eb");
    public static final Color CHECKPOINT = Color.web("8e4793");
    public static final Color TELEPORT = Color.web("#214764");
    public static final Color FOREGROUND = Color.web("dddddd");
}
