package app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
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
import think.repr.Problem;
import think.repr.Problem.InvalidSpecException;

/**
    Application launch point. Connects the GUI to the Solver.
 */
public final class Main extends Application {

    private static final String NAME = "Tridwoxi's Pathery AI";

    // There's only one Main instance, so everything may as well be static. It makes
    // access easier when you don't keep track of instances.
    private static Gui gui;
    private static Scene scene;
    private static Stage stage;

    @Override
    public void start(final Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            Platform.exit();
        });
        Main.gui = new Gui();
        Main.stage = primaryStage;
        Main.scene = new Scene(gui);
        scene.setFill(Color.GRAY);
        primaryStage.setScene(scene);
        primaryStage.setTitle(NAME);
        primaryStage.setOnCloseRequest(event -> {
            Solver.stop();
            Platform.exit();
        });
        primaryStage.show();
    }

    // == Connectors. ==================================================================

    public static void send(final File file) {
        Problem problem = null;
        try {
            problem = new Problem(Files.readString(file.toPath()));
        } catch (InvalidSpecException e) {
            System.err.println("Bad specification.");
        } catch (IOException e) {
            System.err.println("Can't read file.");
        }
        if (problem != null) {
            Solver.solve(problem);
            stage.setTitle(file.getName());
        }
    }

    public static void recieve(
        final Problem problem,
        final HashSet<Cell> rubbers,
        final int score
    ) {
        Platform.runLater(() -> gui.showUpdate(problem, rubbers, score));
    }
}

final class Gui extends VBox {

    private static final double CELL_SIZE = 50.0; // Pixels.
    private static final double PADDING = 50.0;
    private static final double SPACING = 50.0;

    private ProblemDisplay problemDisplay;
    private final Text scoreDisplay;

    Gui() {
        super(SPACING);
        this.problemDisplay = new ProblemDisplay();
        this.scoreDisplay = new Text();
        scoreDisplay.setFill(PatheryColors.FOREGROUND);

        setBackground(Background.fill(PatheryColors.BACKGROUND));
        setPadding(new Insets(PADDING));
        setAlignment(Pos.TOP_CENTER);
        final HBox stats = new HBox(SPACING, scoreDisplay, makeButton());
        stats.setAlignment(Pos.CENTER);
        getChildren().addAll(problemDisplay, stats);
    }

    public void showUpdate(
        final Problem problem,
        final HashSet<Cell> rubbers,
        final int score
    ) {
        problemDisplay = new ProblemDisplay(problem, CELL_SIZE, rubbers);
        scoreDisplay.setText("Score: " + score);
        assert getChildren().get(0) instanceof ProblemDisplay;
        getChildren().set(0, problemDisplay);
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
                .add(new ExtensionFilter("Pathery level specification", "*.tpai"));
            final Window active = getScene() == null ? null : getScene().getWindow();
            final File chosen = chooser.showOpenDialog(active);
            if (chosen != null) {
                Main.send(chosen);
            }
        });
        return upload;
    }
}

final class ProblemDisplay extends Group {

    ProblemDisplay() {}

    ProblemDisplay(
        final Problem problem,
        final double cellSize,
        final HashSet<Cell> rubbers
    ) {
        assert cellSize > 0.0;
        assert rubbers.size() <= problem.getNumRubbers();
        final ArrayList<Cell> checks = problem.getCheckpoints();
        final HashMap<Cell, Integer> checkLabels = new HashMap<>();
        for (int i = 0; i < checks.size(); i++) {
            checkLabels.put(checks.get(i), i);
        }
        for (final Cell cell : problem.getAllCells()) {
            final int label = checkLabels.getOrDefault(cell, -1);
            final Color color;
            if (label != -1) {
                color = PatheryColors.CHECKPOINT;
            } else if (problem.isBrick(cell)) {
                color = PatheryColors.BRICK;
            } else if (rubbers.contains(cell)) {
                color = PatheryColors.RUBBER;
            } else {
                color = PatheryColors.NOTHING;
            }
            final String content = label != -1 ? Integer.toString(label) : "";
            final CellDisplay cellDisplay = new CellDisplay(color, cellSize, content);
            cellDisplay.setLayoutY(cell.i() * cellSize);
            cellDisplay.setLayoutX(cell.j() * cellSize);
            getChildren().add(cellDisplay);
        }
    }
}

final class CellDisplay extends Group {

    private static final double HALF = 0.5;

    CellDisplay(final Color color, final double size, final String content) {
        final Rectangle rect = new Rectangle(size, size);
        rect.setFill(color);
        rect.setStroke(PatheryColors.BACKGROUND);
        if (content.isBlank()) {
            getChildren().add(rect);
            return;
        }
        final Text label = new Text(content);
        label.setFill(PatheryColors.FOREGROUND);
        label.setFont(Font.font(size * HALF));
        final Bounds bounds = label.getLayoutBounds();
        label.setX((size - bounds.getWidth()) * HALF - bounds.getMinX());
        label.setY((size - bounds.getHeight()) * HALF - bounds.getMinY());
        getChildren().addAll(rect, label);
    }
}

final class PatheryColors {

    // Approximate average color, picked from Pathery.com. As I add more features, I
    // hope to use all these colors, but for now most are extra.
    public static final Color BACKGROUND = Color.web("121212");
    public static final Color BRICK = Color.web("723736");
    public static final Color RUBBER = Color.web("3c3d3c");
    public static final Color NOTHING = Color.web("e2e8eb");
    public static final Color CHECKPOINT = Color.web("8e4793");
    public static final Color TELEPORT = Color.web("#214764");
    public static final Color FOREGROUND = Color.web("dddddd");

    private PatheryColors() {}
}
