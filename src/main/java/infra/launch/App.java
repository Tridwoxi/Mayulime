package infra.launch;

import infra.gui.Gui;
import infra.output.Logging;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import think.domain.codec.Parser;
import think.domain.codec.Parser.BadMapCodeException;
import think.domain.model.Puzzle;
import think.manager.Manager;
import think.manager.StatusUpdate;

/**
    Normal application launch point. Connects Gui (frontend) to Manager (backend).
 */
public final class App extends Application {

    private static final String UNNAMED_PROBLEM_NAME = "Unnamed Problem";
    private static final String BAD_MAP_MESSAGE =
        "Unable to parse that file as supported Pathery MapCode.";
    private static final double MIN_WIDTH_PX = 480.0;
    private static final double MIN_HEIGHT_PX = 320.0;
    private static final double DEFAULT_WIDTH_PX = 1280;
    private static final double DEFAULT_HEIGHT_PX = 720.0;

    private final AtomicInteger puzzleEpoch;

    private Manager manager;
    private Gui gui;

    public App() {
        this.puzzleEpoch = new AtomicInteger(0);
        this.manager = null;
        this.gui = null;
    }

    @Override
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            exception.printStackTrace();
            Platform.exit();
        });
    }

    @Override
    public void start(final Stage primaryStage) {
        Logging.announcement("Launch point: Application");

        this.manager = new Manager(this::receiveSolution);
        this.gui = new Gui(this::receiveMapCode, this::stopRequestedByUser);

        primaryStage.setScene(gui);
        primaryStage.setMinWidth(MIN_WIDTH_PX);
        primaryStage.setMinHeight(MIN_HEIGHT_PX);
        primaryStage.setWidth(DEFAULT_WIDTH_PX);
        primaryStage.setHeight(DEFAULT_HEIGHT_PX);
        primaryStage.show();
    }

    // == Connectors. =============================================================================

    private void receiveSolution(final StatusUpdate update) {
        if (gui == null || manager == null) {
            throw new IllegalStateException();
        }
        final int epoch = this.puzzleEpoch.get();
        gui.enqueueSolverUpdate(update, epoch);
    }

    private void receiveMapCode(final String mapCode) {
        if (gui == null || manager == null) {
            throw new IllegalStateException();
        }

        final Puzzle puzzle;
        try {
            puzzle = Parser.parse(mapCode);
        } catch (BadMapCodeException exception) {
            Logging.warning("Bad MapCode or unsupported feature; problem rejected");
            gui.onMapCodeRejected(BAD_MAP_MESSAGE);
            return;
        }

        manager.stop();

        final int epoch = this.puzzleEpoch.incrementAndGet();
        final String problemName = puzzle.getName().isBlank()
            ? UNNAMED_PROBLEM_NAME
            : puzzle.getName();
        gui.onPuzzleAccepted(
            new Puzzle(
                problemName,
                puzzle.getFeatures(),
                puzzle.getNumRows(),
                puzzle.getNumCols(),
                puzzle.getCheckpoints(),
                puzzle.getBlockingBudget()
            ),
            epoch
        );
        manager.solve(puzzle);
    }

    private void stopRequestedByUser() {
        if (gui == null || manager == null) {
            throw new IllegalStateException();
        }
        manager.stop();
        final int epoch = this.puzzleEpoch.incrementAndGet();
        gui.onPuzzleStopped(epoch, "Solving stopped");
    }
}
