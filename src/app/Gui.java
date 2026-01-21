package app;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;
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
import think.Manager.Strategy;
import think.ana.Tools;
import think.repr.Cell;
import think.repr.Problem;

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
        final HashSet<Cell> playerWalls,
        final int score
    ) {
        showGame();
        gameDisplay.setGame(problem, playerWalls);
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

    private record ColorString(Color color, String string) {}

    GameDisplay() {}

    public void setGame(final Problem problem, final HashSet<Cell> playerWalls) {
        final HashMap<Cell, Integer> checkpoints = new HashMap<>();
        Tools.enumerate(problem.getCheckpoints()).forEachOrdered(uniordered ->
            checkpoints.put(uniordered.item(), uniordered.order1())
        );

        final Function<Cell, ColorString> getColorString = cell -> {
            // These cases must be disjoint, but it is not our responsibility to check.
            if (problem.isSystemWall(cell)) {
                return new ColorString(PatheryColors.SYSTEM_WALL, "");
            }
            if (checkpoints.containsKey(cell)) {
                final String content = checkpoints.get(cell).toString();
                return new ColorString(PatheryColors.CHECKPOINT, content);
            }
            if (playerWalls.contains(cell)) {
                return new ColorString(PatheryColors.PLAYER_WALL, "");
            }
            return new ColorString(PatheryColors.EMPTY, "");
        };

        getChildren().clear();
        for (Cell cell : problem.getAllCells()) {
            final ColorString colorString = getColorString.apply(cell);
            final CellDisplay cellDisplay = new CellDisplay(
                colorString.color(),
                colorString.string()
            );
            cellDisplay.setLayoutY(cell.row() * Gui.CELL_SIZE_PX);
            cellDisplay.setLayoutX(cell.col() * Gui.CELL_SIZE_PX);
            getChildren().add(cellDisplay);
        }
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
    private static final String FORMAT_NAME = "Pathery level specification";
    private static final String FORMAT_EXT = "*.tpai";

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
            Main.getInstance().send(chosen);
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
