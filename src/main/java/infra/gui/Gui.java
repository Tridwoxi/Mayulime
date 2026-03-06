package infra.gui;

import java.util.function.Consumer;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.scene.Scene;
import think.domain.model.Puzzle;
import think.manager.StatusUpdate;

public final class Gui extends Scene {

    static final String FONT_NAME = "Helvetica";
    static final double MIN_CELL_SIZE_PX = 10.0;
    static final double MAX_CELL_SIZE_PX = 50.0;

    private final UiController controller;
    private ColorScheme activeColorScheme;

    public Gui(final Consumer<String> mapCodeConsumer, final Runnable stopConsumer) {
        this(new UiController(mapCodeConsumer, stopConsumer));
    }

    private Gui(final UiController controller) {
        super(controller.getRoot());
        this.controller = controller;
        this.activeColorScheme = Platform.getPreferences().getColorScheme();

        this.controller.applyColorScheme(this.activeColorScheme);
        Platform.getPreferences().colorSchemeProperty().addListener((ignored, oldValue, newValue) ->
            this.applyColorScheme(newValue)
        );
    }

    public void onPuzzleAccepted(final Puzzle puzzle, final int puzzleEpoch) {
        controller.onPuzzleAccepted(puzzle, puzzleEpoch);
    }

    public void onPuzzleRejected(final int puzzleEpoch, final String message) {
        controller.onPuzzleRejected(puzzleEpoch, message);
    }

    public void onPuzzleStopped(final int puzzleEpoch, final String message) {
        controller.onPuzzleStopped(puzzleEpoch, message);
    }

    public void enqueueSolverUpdate(final StatusUpdate update, final int puzzleEpoch) {
        controller.enqueueSolverUpdate(update, puzzleEpoch);
    }

    private void applyColorScheme(final ColorScheme colorScheme) {
        if (colorScheme == this.activeColorScheme) {
            return;
        }
        this.activeColorScheme = colorScheme;
        this.controller.applyColorScheme(colorScheme);
    }
}
