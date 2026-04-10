package infra.gui;

import java.util.function.Consumer;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import think.domain.model.Puzzle;
import think.solvers.SolverKind;

public final class Gui extends Scene {

    static final String FONT_NAME = "Helvetica";
    static final double MIN_CELL_SIZE_PX = 10.0;
    static final double MAX_CELL_SIZE_PX = 50.0;

    private static final double MIN_WIDTH_PX = 480.0;
    private static final double MIN_HEIGHT_PX = 320.0;
    private static final double DEFAULT_WIDTH_PX = 1280.0;
    private static final double DEFAULT_HEIGHT_PX = 720.0;

    private final UiController controller;
    private ColorScheme activeColorScheme;

    public Gui(final Consumer<String> mapCodeConsumer, final Runnable stopConsumer) {
        this(new UiController(mapCodeConsumer, stopConsumer));
    }

    private Gui(final UiController controller) {
        super(controller.getRoot());
        this.controller = controller;
        this.activeColorScheme = Platform.getPreferences().getColorScheme();

        this.setFill(paletteBackground(this.activeColorScheme));
        this.controller.applyColorScheme(this.activeColorScheme);
        Platform.getPreferences()
            .colorSchemeProperty()
            .addListener((_, _, newValue) -> this.applyColorScheme(newValue));
    }

    public void onPuzzleAccepted(final Puzzle puzzle, final int puzzleEpoch) {
        controller.onPuzzleAccepted(puzzle, puzzleEpoch);
    }

    public void onPuzzleRejected(final int puzzleEpoch, final String message) {
        controller.onPuzzleRejected(puzzleEpoch, message);
    }

    public void onMapCodeRejected(final String message) {
        controller.onMapCodeRejected(message);
    }

    public void onPuzzleStopped(final int puzzleEpoch, final String message) {
        controller.onPuzzleStopped(puzzleEpoch, message);
    }

    public void enqueueSolverUpdate(final Submission update, final int puzzleEpoch) {
        controller.enqueueSolverUpdate(update, puzzleEpoch);
    }

    public SolverKind getSolverKind() {
        return controller.getSolverKind();
    }

    public int getThreadCount() {
        return controller.getThreadCount();
    }

    public static void configureStage(final Stage stage) {
        stage.setMinWidth(MIN_WIDTH_PX);
        stage.setMinHeight(MIN_HEIGHT_PX);
        stage.setWidth(DEFAULT_WIDTH_PX);
        stage.setHeight(DEFAULT_HEIGHT_PX);
        stage.show();
    }

    private void applyColorScheme(final ColorScheme colorScheme) {
        if (colorScheme == this.activeColorScheme) {
            return;
        }
        this.activeColorScheme = colorScheme;
        this.setFill(paletteBackground(colorScheme));
        this.controller.applyColorScheme(colorScheme);
    }

    private static Color paletteBackground(final ColorScheme colorScheme) {
        return UiPalette.fromColorScheme(colorScheme).background();
    }
}
