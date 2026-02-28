package infra.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
import think2.domain.repr.Display;
import think2.graph.impl.GridGraph.Cell;

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
    static final double MIN_CELL_SIZE_PX = 8.0;
    static final double MAX_CELL_SIZE_PX = 50.0;
    static final double SPACING_PX = 50.0;
    static final double PADDING_PX = 50.0;

    private final Consumer<String> mapCodeConsumer;
    private final VBox root;
    private final GameDisplay gameDisplay;
    private final StatsDisplay statsDisplay;
    private double currentCellSizePx;

    public Gui(final Consumer<String> mapCodeConsumer) {
        super(new VBox());
        this.mapCodeConsumer = mapCodeConsumer;
        this.root = (VBox) getRoot();
        this.gameDisplay = new GameDisplay(this);
        this.statsDisplay = new StatsDisplay(this);
        this.currentCellSizePx = MAX_CELL_SIZE_PX;

        setFill(PatheryColors.BACKGROUND);
        root.setPadding(new Insets(PADDING_PX));
        root.setAlignment(Pos.CENTER);
        root.setSpacing(0.0);
        root.setBackground(Background.fill(PatheryColors.BACKGROUND));
        root.getChildren().addAll(gameDisplay, statsDisplay);
        widthProperty().addListener((ignored, oldValue, newValue) -> handleResize());
        heightProperty().addListener((ignored, oldValue, newValue) -> handleResize());
        hideGame();
    }

    public void update(final Display display) {
        showGame();
        statsDisplay.setScore(display.getScore(), display.getSubmitter());
        recalculateCellSize(display);
        gameDisplay.setGame(display);
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

    Consumer<String> getMapCodeConsumer() {
        return mapCodeConsumer;
    }

    double getCurrentCellSizePx() {
        return currentCellSizePx;
    }

    private void handleResize() {
        // PERF: Noticeable lag on large (100 by 100) maps on a modern computer.
        recalculateCellSize(gameDisplay.getCurrentDisplay());
        gameDisplay.rerenderIfPresent();
    }

    private void recalculateCellSize(final Display display) {
        if (display == null) {
            currentCellSizePx = MAX_CELL_SIZE_PX;
            return;
        }
        final int numRows = display.getNumRows();
        final int numCols = display.getNumCols();
        if (numRows <= 0 || numCols <= 0) {
            currentCellSizePx = MAX_CELL_SIZE_PX;
            return;
        }

        final Insets insets = root.getPadding();
        final double availableWidth = Math.max(
            1.0,
            getWidth() - insets.getLeft() - insets.getRight()
        );
        double availableHeight = Math.max(1.0, getHeight() - insets.getTop() - insets.getBottom());
        if (gameDisplay.isVisible()) {
            final double statsHeight = Math.max(
                statsDisplay.getLayoutBounds().getHeight(),
                statsDisplay.prefHeight(-1.0)
            );
            availableHeight -= statsHeight;
            availableHeight -= root.getSpacing();
        }
        availableHeight = Math.max(1.0, availableHeight);

        final double sizeByWidth = availableWidth / numCols;
        final double sizeByHeight = availableHeight / numRows;
        final double rawSize = Math.min(sizeByWidth, sizeByHeight);
        currentCellSizePx = Math.max(MIN_CELL_SIZE_PX, Math.min(MAX_CELL_SIZE_PX, rawSize));
    }
}

final class GameDisplay extends Group {

    private final Gui gui;
    private Display currentDisplay;

    GameDisplay(final Gui gui) {
        this.gui = gui;
    }

    public void setGame(final Display display) {
        currentDisplay = display;
        render();
    }

    public void rerenderIfPresent() {
        if (currentDisplay != null) {
            render();
        }
    }

    public Display getCurrentDisplay() {
        return currentDisplay;
    }

    private void render() {
        if (currentDisplay == null) {
            throw new IllegalStateException();
        }
        getChildren().clear();
        final double cellSizePx = gui.getCurrentCellSizePx();
        currentDisplay
            .getAllCells()
            .stream()
            .forEachOrdered(cell -> {
                final Display.Kind kind = currentDisplay.getKind(cell);
                final CellDisplay cellDisplay = new CellDisplay(
                    toColor(kind),
                    currentDisplay.getName(cell),
                    cellSizePx
                );
                cellDisplay.setLayoutY(cell.row() * cellSizePx);
                cellDisplay.setLayoutX(cell.col() * cellSizePx);
                getChildren().add(cellDisplay);
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

final class CellDisplay extends Group {

    private static final double LABLE_SCALE = 0.3;
    private static final double HALF = 0.5;

    CellDisplay(final Color color, final String content, final double sizePx) {
        final Rectangle body = new Rectangle(sizePx, sizePx);
        body.setFill(color);
        body.setStroke(PatheryColors.BACKGROUND);

        final Text label = new Text(content);
        label.setFill(PatheryColors.FOREGROUND);

        label.setFont(Font.font(Gui.FONT_NAME, sizePx * LABLE_SCALE));

        final Bounds bounds = label.getLayoutBounds();
        label.setX((sizePx - bounds.getWidth()) * HALF - bounds.getMinX());
        label.setY((sizePx - bounds.getHeight()) * HALF - bounds.getMinY());

        getChildren().addAll(body, label);
    }
}

final class StatsDisplay extends HBox {

    private final ScoreDisplay scoreDisplay;
    private final ButtonDisplay buttonDisplay;

    StatsDisplay(final Gui gui) {
        this.scoreDisplay = new ScoreDisplay(gui);
        this.buttonDisplay = new ButtonDisplay(gui);
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

    ScoreDisplay(final Gui gui) {
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
    private final Gui gui;

    ButtonDisplay(final Gui gui) {
        super(TEXT);
        this.gui = gui;
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
        if (chosen == null) {
            Logging.info("File picker cancelled.");
            return;
        }
        Logging.info("Picked file %s", chosen.getPath());

        String mapCode = null;
        try {
            mapCode = Files.readString(chosen.toPath());
        } catch (IOException ignored) {
            Logging.warning("Can't read file (!?)");
        }
        if (mapCode != null) {
            gui.getMapCodeConsumer().accept(mapCode);
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
    public static final Color FOREGROUND = Color.web("dddddd");

    private PatheryColors() {}
}
