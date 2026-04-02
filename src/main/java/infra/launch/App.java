package infra.launch;

import infra.gui.Gui;
import infra.gui.Submission;
import infra.output.Logging;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import think.domain.codec.Parser;
import think.domain.codec.Parser.BadMapCodeException;
import think.domain.codec.Serializer;
import think.domain.model.Puzzle;
import think.manager.Manager;
import think.manager.Proposal;
import think.solvers.SolverKind;

/**
    Normal application launch point. Connects Gui (frontend) to Manager (backend).
 */
public final class App extends Application {

    private static final int UNSCORED = -2; // StandardEvaluator uses -1.
    private static final String BAD_MAP_MESSAGE =
        "Unable to parse that file as supported Pathery MapCode.";
    private static final int NUM_BEST_SOLVERS = 10;
    private static final List<SolverKind> SOLVER_KINDS;

    static {
        final ArrayList<SolverKind> kinds = new ArrayList<>(1 + NUM_BEST_SOLVERS);
        kinds.add(SolverKind.BASELINE);
        kinds.addAll(Collections.nCopies(NUM_BEST_SOLVERS, SolverKind.getBest()));
        SOLVER_KINDS = List.copyOf(kinds);
    }

    private final AtomicInteger puzzleEpoch;
    private volatile int topScore;
    private volatile String currentPuzzleName;

    private Manager manager;
    private Gui gui;

    public App() {
        this.puzzleEpoch = new AtomicInteger(0);
        this.topScore = UNSCORED;
        this.currentPuzzleName = Parser.UNNAMED_PUZZLE_NAME;
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

        this.manager = new Manager(this::receiveProposal, SOLVER_KINDS);
        this.gui = new Gui(this::receiveMapCode, this::stopRequestedByUser);

        primaryStage.setScene(gui);
        Gui.configureStage(primaryStage);
    }

    // == Connectors. ==

    private synchronized void receiveProposal(final Proposal proposal) {
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
        Logging.info(
            "MapCode for score %d: %s",
            update.getScore(),
            Serializer.serialize(proposal.getPuzzle(), proposal.getFeatures())
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
        this.currentPuzzleName = puzzle.getName();
        gui.onPuzzleAccepted(puzzle, epoch);
        manager.solve(puzzle);
    }

    private void stopRequestedByUser() {
        if (gui == null || manager == null) {
            throw new IllegalStateException();
        }
        manager.stop();
        this.topScore = UNSCORED;
        this.currentPuzzleName = Parser.UNNAMED_PUZZLE_NAME;
        final int epoch = this.puzzleEpoch.incrementAndGet();
        gui.onPuzzleStopped(epoch, "Solving stopped");
    }
}
