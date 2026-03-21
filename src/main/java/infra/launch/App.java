package infra.launch;

import infra.gui.Gui;
import infra.gui.Submission;
import infra.output.Logging;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import think.domain.codec.Parser;
import think.domain.codec.Parser.BadMapCodeException;
import think.domain.model.Puzzle;
import think.manager.Manager;
import think.manager.Proposal;
import think.solvers.SolverKind;

/**
    Normal application launch point. Connects Gui (frontend) to Manager (backend).
 */
public final class App extends Application {

    private static final int UNSCORED = -2; // StandardEvaluator uses -1.
    private static final String UNNAMED_PROBLEM_NAME = "Unnamed Problem";
    private static final String BAD_MAP_MESSAGE =
        "Unable to parse that file as supported Pathery MapCode.";
    private static final double MIN_WIDTH_PX = 480.0;
    private static final double MIN_HEIGHT_PX = 320.0;
    private static final double DEFAULT_WIDTH_PX = 1280;
    private static final double DEFAULT_HEIGHT_PX = 720.0;
    private static final List<SolverKind> DEFAULT_SOLVER_KINDS = SolverKind.asList();

    private final AtomicInteger puzzleEpoch;
    private volatile int topScore;
    private volatile String currentPuzzleName;

    private Manager manager;
    private Gui gui;

    public App() {
        this.puzzleEpoch = new AtomicInteger(0);
        this.topScore = UNSCORED;
        this.currentPuzzleName = UNNAMED_PROBLEM_NAME;
        this.manager = null;
        this.gui = null;
    }

    @Override
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler((_, exception) -> {
            exception.printStackTrace();
            Platform.exit();
        });
    }

    @Override
    public void start(final Stage primaryStage) {
        Logging.announcement("Launch point: Application");

        this.manager = new Manager(this::receiveSolution, DEFAULT_SOLVER_KINDS);
        this.gui = new Gui(this::receiveMapCode, this::stopRequestedByUser);

        primaryStage.setScene(gui);
        primaryStage.setMinWidth(MIN_WIDTH_PX);
        primaryStage.setMinHeight(MIN_HEIGHT_PX);
        primaryStage.setWidth(DEFAULT_WIDTH_PX);
        primaryStage.setHeight(DEFAULT_HEIGHT_PX);
        primaryStage.show();
    }

    // == Connectors. ==

    private synchronized void receiveSolution(final Proposal proposal) {
        if (gui == null || manager == null) {
            throw new IllegalStateException();
        }
        final Submission update = new Submission(
            proposal.getSubmitter(),
            proposal.getPuzzle(),
            proposal.getFeatures(),
            proposal.getScore()
        );
        final int epoch = this.puzzleEpoch.get();
        if (update.getScore() <= this.topScore) {
            return;
        }
        final String priorScoreText =
            this.topScore == UNSCORED ? "Unscored" : Integer.toString(this.topScore);
        Logging.info(
            "Score %s -> %d on %s by %s",
            priorScoreText,
            update.getScore(),
            this.currentPuzzleName,
            update.getSubmitter()
        );
        this.topScore = update.getScore();
        this.gui.enqueueSolverUpdate(update, epoch);
    }

    private void receiveMapCode(final String mapCode) {
        if (gui == null || manager == null) {
            throw new IllegalStateException();
        }

        final Puzzle puzzle;
        try {
            puzzle = Parser.parse(mapCode);
        } catch (BadMapCodeException _) {
            Logging.warning("Bad MapCode or unsupported feature; problem rejected");
            gui.onMapCodeRejected(BAD_MAP_MESSAGE);
            return;
        }

        manager.stop();
        this.topScore = UNSCORED;

        final int epoch = this.puzzleEpoch.incrementAndGet();
        final String problemName = puzzle.getName().isBlank()
            ? UNNAMED_PROBLEM_NAME
            : puzzle.getName();
        this.currentPuzzleName = problemName;
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
        this.topScore = UNSCORED;
        this.currentPuzzleName = UNNAMED_PROBLEM_NAME;
        final int epoch = this.puzzleEpoch.incrementAndGet();
        gui.onPuzzleStopped(epoch, "Solving stopped");
    }
}
