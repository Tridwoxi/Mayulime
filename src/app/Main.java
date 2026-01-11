package app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
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
import think.Solver;
import think.repr.Cell;
import think.repr.Cell.CellType;
import think.repr.Parser;
import think.repr.Point;
import think.repr.Problem;

public final class Main extends Application {

    private static Gui gui;
    private static Scene mainScene;
    private static Stage primaryStage;

    @Override
    public void start(final Stage primaryStage) {
        Main.gui = new Gui();
        Main.primaryStage = primaryStage;
        Main.mainScene = new Scene(gui);
        mainScene.setFill(Color.GRAY);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Pathery Solver");
        primaryStage.show();
    }

    public static void toSolver(final File file) {
        Problem board = null;
        try {
            board = Parser.parse(Files.readString(file.toPath()));
        } catch (IllegalArgumentException e) {
            System.err.println("Bad specification: " + e);
        } catch (IOException e) {
            System.err.println("Can't read file.");
        }
        if (board != null) {
            Solver.solve(board);
            primaryStage.setTitle(file.getName());
        }
    }

    public static void fromSolver(
        final Problem board,
        final HashSet<Point> rubbers,
        final int score
    ) {
        Platform.runLater(() -> gui.showUpdate(board, rubbers, score));
    }
}

final class Gui extends VBox {

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
        final HBox stats = new HBox(SPACING, scoreDisplay, rubberDisplay, makeButton());
        stats.setAlignment(Pos.CENTER);
        getChildren().addAll(boardDisplay, stats);
    }

    public void showUpdate(
        final Problem board,
        final HashSet<Point> rubberAssignment,
        final int score
    ) {
        boardDisplay = new BoardDisplay(board, CELL_SIZE, rubberAssignment);
        scoreDisplay.setText("Score: " + score);
        final int remaining = board.getNumRubbers() - rubberAssignment.size();
        rubberDisplay.setText("Remaining walls: " + remaining);
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
        final Button upload = new Button("Upload problem");
        upload.setBackground(Background.fill(PatheryColors.BACKGROUND));
        upload.setBorder(
            new Border(
                new BorderStroke(
                    PatheryColors.FOREGROUND,
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    BorderStroke.THIN
                )
            )
        );
        upload.setTextFill(PatheryColors.FOREGROUND);
        upload.setOnAction(event -> {
            final FileChooser chooser = new FileChooser();
            chooser
                .getExtensionFilters()
                .add(new ExtensionFilter("Pathery level specification", "*.txt"));
            final Window active = getScene() == null ? null : getScene().getWindow();
            final File chosen = chooser.showOpenDialog(active);
            if (chosen != null) {
                Main.toSolver(chosen);
            }
        });
        return upload;
    }
}

final class BoardDisplay extends Group {

    public BoardDisplay() {}

    public BoardDisplay(
        final Problem board,
        final double cellSize,
        final HashSet<Point> rubberAssignment
    ) {
        for (final Point point : board.getEverything()) {
            final Cell cellData = board.isBrick(point);
            final boolean hasRubber = rubberAssignment.contains(point);
            final CellDisplay cell = new CellDisplay(cellData, cellSize, hasRubber);
            cell.setLayoutY(point.i() * cellSize);
            cell.setLayoutX(point.j() * cellSize);
            getChildren().add(cell);
            if (cellData.type() != CellType.NOTHING && hasRubber) {
                System.err.println("Internal error: illegal assignment.");
                Platform.exit();
            }
        }
    }
}

final class CellDisplay extends Group {

    public CellDisplay(final Cell cell, final double cellSize, final boolean hasRubber) {
        final Rectangle rect = new Rectangle(cellSize, cellSize);
        rect.setFill(getColor(cell, hasRubber));
        rect.setStroke(PatheryColors.BACKGROUND);
        if (
            cell.type() == Cell.CellType.CHECKPOINT ||
            cell.type() == Cell.CellType.TELEPORT_IN ||
            cell.type() == Cell.CellType.TELEPORT_OUT
        ) {
            final Text label = new Text(String.valueOf(cell.association()));
            label.setFill(PatheryColors.FOREGROUND);
            label.setFont(Font.font(cellSize * 0.5));
            final Bounds bounds = label.getLayoutBounds();
            label.setX((cellSize - bounds.getWidth()) * 0.5 - bounds.getMinX());
            label.setY((cellSize - bounds.getHeight()) * 0.5 - bounds.getMinY());
            getChildren().addAll(rect, label);
        } else {
            getChildren().add(rect);
        }
    }

    private Color getColor(final Cell cell, final boolean hasRubber) {
        if (hasRubber) {
            return PatheryColors.RUBBER;
        }
        return switch (cell.type()) {
            case BRICK -> PatheryColors.BRICK;
            case CHECKPOINT -> PatheryColors.CHECKPOINT;
            case NOTHING -> PatheryColors.NOTHING;
            case TELEPORT_IN -> PatheryColors.TELEPORT;
            case TELEPORT_OUT -> PatheryColors.TELEPORT;
        };
    }
}

final class PatheryColors {

    // Approximate average color, picked from Pathery.com
    public static final Color BACKGROUND = Color.web("121212");
    public static final Color BRICK = Color.web("723736");
    public static final Color RUBBER = Color.web("3c3d3c");
    public static final Color NOTHING = Color.web("e2e8eb");
    public static final Color CHECKPOINT = Color.web("8e4793");
    public static final Color TELEPORT = Color.web("#214764");
    public static final Color FOREGROUND = Color.web("dddddd");

    private PatheryColors() {}
}
