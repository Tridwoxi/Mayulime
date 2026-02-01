package app;

import java.io.File;
import java.util.HashMap;
import java.util.function.Consumer;
import javafx.event.Event;
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
import javafx.stage.Window;
import think.repr.Cell;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;
import think.stra.Strategy;
import think.tools.Iteration;
import think.tools.Iteration.Pair;

/**
    Display the game and its stats. Scene graph:

    <pre>
    root (VBox)
        GameDisplay (Group)
            CellDisplay (Group)
                body (Rectangle)
                label (Text)
        StatsDisplay (Hbox)
            ScoreDisplay (Text)
            ButtonDisplay (Button)
    </pre>
 */
public final class Gui extends Scene {

    static final String FONT_NAME = "System";
    static final double CELL_SIZE_PX = 50.0;
    static final double SPACING_PX = 50.0;
    static final double PADDING_PX = 50.0;
    private final VBox root;
    private final GameDisplay gameDisplay;
    private final StatsDisplay statsDisplay;

    public Gui() {
        super(new VBox());
        this.root = (VBox) getRoot();
        this.gameDisplay = new GameDisplay();
        this.statsDisplay = new StatsDisplay();

        setFill(PatheryColors.BACKGROUND);
        root.setPadding(new Insets(PADDING_PX));
        root.setAlignment(Pos.CENTER);
        root.setSpacing(0.0);
        root.setBackground(Background.fill(PatheryColors.BACKGROUND));
        root.getChildren().addAll(gameDisplay, statsDisplay);
        hideGame();
    }

    public void update(
        final Strategy submitter,
        final Problem problem,
        final Grid<Feature> solution,
        final int score
    ) {
        showGame();
        gameDisplay.setGame(problem, solution);
        statsDisplay.setScore(score, submitter.getName());
    }

    private void hideGame() {
        gameDisplay.setManaged(false);
        gameDisplay.setVisible(false);
        root.setSpacing(0.0);
    }

    private void showGame() {
        gameDisplay.setManaged(true);
        gameDisplay.setVisible(true);
        root.setSpacing(SPACING_PX);
    }
}

final class GameDisplay extends Group {

    // These labels can be anything, but using whatever code the Pathery MapCode uses
    // is probably most understandable. To avoid clutter, only these cell types are
    // labeled. Like, labeling system walls would just be noise.
    private static final String TELEPORT_IN = "t";
    private static final String TELEPORT_OUT = "u";
    private static final String CHECKPOINT = "c";

    GameDisplay() {}

    public void setGame(final Problem problem, final Grid<Feature> solution) {
        getChildren().clear();
        final Grid<String> labels = makeLabels(problem);
        solution
            .stream()
            .forEachOrdered(pair -> {
                final Feature feature = pair.first();
                final Cell cell = pair.second();
                final CellDisplay cellDisplay = new CellDisplay(
                    toColor(feature),
                    labels.get(cell)
                );
                cellDisplay.setLayoutY(cell.row() * Gui.CELL_SIZE_PX);
                cellDisplay.setLayoutX(cell.col() * Gui.CELL_SIZE_PX);
                getChildren().add(cellDisplay);
            });
    }

    private static Color toColor(final Feature feature) {
        return switch (feature) {
            case EMPTY -> PatheryColors.EMPTY;
            case CHECKPOINT -> PatheryColors.CHECKPOINT;
            case SYSTEM_WALL -> PatheryColors.SYSTEM_WALL;
            case PLAYER_WALL -> PatheryColors.PLAYER_WALL;
            case TELEPORT_IN -> PatheryColors.TELEPORT;
            case TELEPORT_OUT -> PatheryColors.TELEPORT;
        };
    }

    private static Grid<String> makeLabels(final Problem problem) {
        // The backend was foolish enough to forget the order of teleports, so we
        // cannot assign teleports their orignal labels. Fortunately, teleports are
        // unordered, so we'll just assign arbritrary associations.
        final int[] association = { 0 }; // Effectively final hack.
        final Grid<String> labels = new Grid<String>(
            "",
            problem.getCachedInitial().getNumRows(),
            problem.getCachedInitial().getNumCols()
        );
        final HashMap<Cell, Integer> checkpoints = new HashMap<>();
        Iteration.enumerate(problem.getCheckpoints()).forEachOrdered(uniordered ->
            checkpoints.put(uniordered.item(), uniordered.order1())
        );
        final HashMap<Cell, Cell> teleports = problem.getTeleports();
        final Consumer<Cell> assign = cell -> {
            switch (problem.getCachedInitial().get(cell)) {
                case TELEPORT_IN -> {
                    assert teleports.containsKey(cell);
                    association[0] += 1;
                    labels.set(cell, TELEPORT_IN + association[0]);
                    labels.set(teleports.get(cell), TELEPORT_OUT + association[0]);
                }
                case CHECKPOINT -> {
                    assert checkpoints.containsKey(cell);
                    labels.set(cell, CHECKPOINT + checkpoints.get(cell));
                }
                default -> {
                    // Do nothing.
                }
            }
        };
        problem.getCachedInitial().stream().map(Pair::second).forEachOrdered(assign);
        return labels;
    }
}

final class CellDisplay extends Group {

    private static final double LABLE_SCALE = 0.3;
    private static final double SIZE = Gui.CELL_SIZE_PX;
    private static final double HALF = 0.5;

    CellDisplay(final Color color, final String content) {
        final Rectangle body = new Rectangle(SIZE, SIZE);
        body.setFill(color);
        body.setStroke(PatheryColors.BACKGROUND);

        final Text label = new Text(content);
        label.setFill(PatheryColors.FOREGROUND);

        label.setFont(Font.font(Gui.FONT_NAME, Gui.CELL_SIZE_PX * LABLE_SCALE));

        final Bounds bounds = label.getLayoutBounds();
        label.setX((SIZE - bounds.getWidth()) * HALF - bounds.getMinX());
        label.setY((SIZE - bounds.getHeight()) * HALF - bounds.getMinY());

        getChildren().addAll(body, label);
    }
}

final class StatsDisplay extends HBox {

    private final ScoreDisplay scoreDisplay;
    private final ButtonDisplay buttonDisplay;

    StatsDisplay() {
        this.scoreDisplay = new ScoreDisplay();
        this.buttonDisplay = new ButtonDisplay();
        setAlignment(Pos.CENTER);
        hideScore();
        setSpacing(0.0);
        getChildren().addAll(scoreDisplay, buttonDisplay);
    }

    public void setScore(final int score, final String submitter) {
        showScore();
        scoreDisplay.setScore(score, submitter);
    }

    private void hideScore() {
        scoreDisplay.setManaged(false);
        scoreDisplay.setVisible(false);
        setSpacing(0.0);
    }

    private void showScore() {
        scoreDisplay.setManaged(true);
        scoreDisplay.setVisible(true);
        setSpacing(Gui.SPACING_PX);
    }
}

final class ScoreDisplay extends Text {

    ScoreDisplay() {
        setFill(PatheryColors.FOREGROUND);
    }

    public void setScore(final int score, final String submitter) {
        setText(String.format("Score %d by %s", score, submitter));
    }
}

final class ButtonDisplay extends Button {

    private static final String TEXT = "Upload problem";
    private static final String FORMAT_NAME = "Pathery MapCode";
    private static final String FORMAT_EXT = "*.mapcode";

    ButtonDisplay() {
        super(TEXT);
        setBackground(Background.fill(PatheryColors.BACKGROUND));
        final BorderStroke stroke = new BorderStroke(
            PatheryColors.FOREGROUND,
            BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY,
            BorderStroke.THIN
        );
        setBorder(new Border(stroke));
        setTextFill(PatheryColors.FOREGROUND);
        setOnAction(this::uploadProblem);
    }

    private void uploadProblem(final Event event) {
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new ExtensionFilter(FORMAT_NAME, FORMAT_EXT));
        final Scene scene = getScene();
        final Window window = scene == null ? null : scene.getWindow();
        final File chosen = chooser.showOpenDialog(window);
        if (chosen != null) {
            App.getInstance().send(chosen);
        }
    }
}

final class PatheryColors {

    // Approximate average color, picked from Pathery.com's default color theme.
    public static final Color BACKGROUND = Color.web("121212");
    public static final Color SYSTEM_WALL = Color.web("723736");
    public static final Color PLAYER_WALL = Color.web("3c3d3c");
    public static final Color EMPTY = Color.web("e2e8eb");
    public static final Color CHECKPOINT = Color.web("8e4793");
    public static final Color TELEPORT = Color.web("#214764");
    public static final Color FOREGROUND = Color.web("dddddd");

    private PatheryColors() {}
}
