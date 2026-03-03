package infra.io;

import infra.io.GuiPanels.Control;
import java.util.Locale;
import java.util.function.Consumer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.util.Duration;
import think.domain.repr.Display;

public final class Gui extends Scene implements Control {

    static final String FONT_NAME = "Roboto";
    static final double MIN_CELL_SIZE_PX = 8.0;
    static final double MAX_CELL_SIZE_PX = 50.0;

    private static final String STATUS_WAITING = "Waiting for map";
    private static final String STATUS_SOLVING = "Solving";
    private static final String STATUS_BEST_UPDATED = "Solving";
    private static final String STATUS_MAP_REJECTED = "Map rejected";

    private final GuiBoard gameDisplay;
    private final GuiPanels panels;
    private final Timeline metricsTicker;

    private Display currentDisplay;
    private double zoomMultiplier;
    private double displayedZoomMultiplier;
    private double currentCellSizePx;
    private int updateCount;

    private String puzzleName;
    private int puzzleRows;
    private int puzzleCols;
    private int wallBudget;

    private String solvingState;
    private String statusMessage;
    private boolean pendingCellSizeReflow;
    private boolean pendingMapRerender;
    private long puzzleStartedAtNanos;
    private long lastUpdateAtNanos;

    public Gui(final Consumer<String> mapCodeConsumer) {
        super(GuiPanels.createRoot());
        this.gameDisplay = new GuiBoard();
        this.panels = new GuiPanels(getRoot(), this.gameDisplay, mapCodeConsumer, this);
        this.metricsTicker = new Timeline(
            new KeyFrame(Duration.seconds(1.0), ignored -> this.refreshUi())
        );

        this.currentDisplay = null;
        this.zoomMultiplier = GuiMath.DEFAULT_ZOOM;
        this.displayedZoomMultiplier = GuiMath.DEFAULT_ZOOM;
        this.currentCellSizePx = MAX_CELL_SIZE_PX;
        this.updateCount = 0;

        this.puzzleName = "Welcome";
        this.puzzleRows = 0;
        this.puzzleCols = 0;
        this.wallBudget = 0;

        this.solvingState = STATUS_WAITING;
        this.statusMessage = "Open, drop, or paste a MapCode to begin";
        this.pendingCellSizeReflow = true;
        this.pendingMapRerender = false;
        this.puzzleStartedAtNanos = -1L;
        this.lastUpdateAtNanos = -1L;

        this.metricsTicker.setCycleCount(Animation.INDEFINITE);
        this.metricsTicker.play();
        this.refreshUi();
    }

    public void update(final Display display) {
        final long nowNanos = System.nanoTime();
        if (this.currentDisplay == null || !this.isSamePuzzle(display)) {
            this.pendingCellSizeReflow = true;
            this.updateCount = 0;
            this.puzzleStartedAtNanos = nowNanos;
        }
        this.currentDisplay = display;
        this.gameDisplay.setGame(display);
        this.pendingMapRerender = true;
        this.puzzleName = display.getPuzzleName().isBlank()
            ? "Unnamed Problem"
            : display.getPuzzleName();
        this.puzzleRows = display.getNumRows();
        this.puzzleCols = display.getNumCols();
        this.wallBudget = display.getWallBudget();

        this.solvingState = STATUS_BEST_UPDATED;
        this.statusMessage = String.format(
            Locale.US,
            "Current score: %d by %s",
            display.getScore(),
            display.getSubmitter()
        );
        this.updateCount += 1;
        this.lastUpdateAtNanos = nowNanos;
        this.refreshUi();
    }

    public void startSolving(
        final String puzzleName,
        final int numRows,
        final int numCols,
        final int wallBudget
    ) {
        this.currentDisplay = null;
        this.gameDisplay.clear();

        this.puzzleName = puzzleName;
        this.puzzleRows = numRows;
        this.puzzleCols = numCols;
        this.wallBudget = wallBudget;
        this.updateCount = 0;
        this.puzzleStartedAtNanos = System.nanoTime();
        this.lastUpdateAtNanos = -1L;

        this.solvingState = STATUS_SOLVING;
        this.statusMessage = "Searching for better solutions";
        this.panels.requestRecenter();
        this.pendingCellSizeReflow = true;
        this.pendingMapRerender = false;
        this.refreshUi();
    }

    public void mapRejected() {
        this.solvingState = STATUS_MAP_REJECTED;
        this.statusMessage = "Unable to parse that file as supported Pathery MapCode.";
        this.puzzleStartedAtNanos = -1L;
        this.lastUpdateAtNanos = -1L;
        this.refreshUi();
    }

    @Override
    public void adjustZoom(final double requestedZoom) {
        this.zoomMultiplier = GuiMath.clampZoom(requestedZoom);
        this.pendingCellSizeReflow = true;
        this.refreshUi();
    }

    @Override
    public double getZoom() {
        return this.zoomMultiplier;
    }

    @Override
    public void onMapSubmissionError(final String message) {
        this.solvingState = STATUS_MAP_REJECTED;
        this.statusMessage = message;
        this.refreshUi();
    }

    @Override
    public void onFilePickerCancelled() {
        this.statusMessage = "File picker cancelled.";
        this.refreshUi();
    }

    private void refreshUi() {
        if (this.pendingCellSizeReflow) {
            this.recalculateCellSize(this.currentDisplay);
            this.pendingCellSizeReflow = false;
            this.pendingMapRerender = true;
        }
        final long nowNanos = System.nanoTime();
        final String elapsed = GuiMath.elapsed(this.puzzleStartedAtNanos, nowNanos);
        final String sinceUpdate = GuiMath.elapsed(this.lastUpdateAtNanos, nowNanos);
        if (this.pendingMapRerender) {
            this.gameDisplay.rerenderIfPresent(this.currentCellSizePx);
            this.pendingMapRerender = false;
        }
        this.panels.render(
            this.currentDisplay,
            this.solvingState,
            this.statusMessage,
            this.puzzleName,
            this.puzzleRows,
            this.puzzleCols,
            this.wallBudget,
            this.updateCount,
            this.displayedZoomMultiplier,
            this.currentCellSizePx,
            sinceUpdate,
            elapsed
        );
    }

    private boolean isSamePuzzle(final Display display) {
        final String displayName = display.getPuzzleName().isBlank()
            ? "Unnamed Problem"
            : display.getPuzzleName();
        return (
            this.puzzleRows == display.getNumRows() &&
            this.puzzleCols == display.getNumCols() &&
            this.wallBudget == display.getWallBudget() &&
            this.puzzleName.equals(displayName)
        );
    }

    private void recalculateCellSize(final Display display) {
        if (display == null) {
            final double fallback = MAX_CELL_SIZE_PX * this.zoomMultiplier;
            this.currentCellSizePx = GuiMath.clampCellSize(fallback);
            this.displayedZoomMultiplier = this.currentCellSizePx / MAX_CELL_SIZE_PX;
            return;
        }

        final int numRows = display.getNumRows();
        final int numCols = display.getNumCols();
        if (numRows <= 0 || numCols <= 0) {
            final double fallback = MAX_CELL_SIZE_PX * this.zoomMultiplier;
            this.currentCellSizePx = GuiMath.clampCellSize(fallback);
            this.displayedZoomMultiplier = this.currentCellSizePx / MAX_CELL_SIZE_PX;
            return;
        }

        final double width = Math.max(1.0, this.panels.getViewportWidth());
        final double height = Math.max(1.0, this.panels.getViewportHeight());

        final double fitByWidth = width / numCols;
        final double fitByHeight = height / numRows;
        final double baseSize = Math.min(fitByWidth, fitByHeight);
        final double maxReachableZoom = MAX_CELL_SIZE_PX / baseSize;
        if (this.zoomMultiplier > maxReachableZoom) {
            this.zoomMultiplier = maxReachableZoom;
        }
        final double rawSize = baseSize * this.zoomMultiplier;
        this.currentCellSizePx = GuiMath.clampCellSize(rawSize);
        this.displayedZoomMultiplier = this.currentCellSizePx / MAX_CELL_SIZE_PX;
    }
}
