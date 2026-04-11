package infra.launch;

import infra.gui.Gui;
import infra.gui.Submission;
import infra.logging.Logger;
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
        Logger.announcement("Launch point: Application");
        this.gui = new Gui(this::receiveMapCode, this::stopRequestedByUser);
        primaryStage.setScene(gui);
        Gui.configureStage(primaryStage);
    }

    // == Connectors. ==

    private synchronized void receiveProposal(final Proposal proposal) {
        if (gui == null) {
            throw new IllegalStateException();
        }
        final Submission update = new Submission(
            proposal.getSubmitter(),
            proposal.getPuzzle(),
            proposal.getState(),
            proposal.getScore()
        );
        final int epoch = this.puzzleEpoch.get();
        if (update.getScore() <= this.topScore) {
            return;
        }
        final String priorScoreText =
            this.topScore == UNSCORED ? "Unscored" : Integer.toString(this.topScore);
        Logger.info(
            "Score %s -> %d on %s by %s",
            priorScoreText,
            update.getScore(),
            this.currentPuzzleName,
            update.getSubmitter()
        );
        Logger.info(
            "MapCode for score %d: %s",
            update.getScore(),
            Serializer.serialize(proposal.getPuzzle(), proposal.getState())
        );
        this.topScore = update.getScore();
        this.gui.enqueueSolverUpdate(update, epoch);
    }

    private void receiveMapCode(final String mapCode) {
        if (gui == null) {
            throw new IllegalStateException();
        }

        final Puzzle puzzle;
        try {
            puzzle = Parser.parse(mapCode);
        } catch (BadMapCodeException _) {
            Logger.warning("Bad MapCode or unsupported tile; problem rejected");
            gui.onMapCodeRejected(BAD_MAP_MESSAGE);
            return;
        }

        if (this.manager != null) {
            this.manager.stop();
            this.manager.close();
        }
        this.topScore = UNSCORED;

        final SolverKind solverKind = gui.getSolverKind();
        final int threadCount = gui.getThreadCount();
        final List<SolverKind> kinds = new ArrayList<>(1 + threadCount);
        kinds.add(SolverKind.BASELINE);
        kinds.addAll(Collections.nCopies(threadCount, solverKind));
        this.manager = new Manager(this::receiveProposal, kinds);

        final int epoch = this.puzzleEpoch.incrementAndGet();
        this.currentPuzzleName = puzzle.getName();
        gui.onPuzzleAccepted(puzzle, epoch);
        manager.solve(puzzle);
    }

    private void stopRequestedByUser() {
        if (this.manager != null) {
            this.manager.stop();
            this.manager.close();
            this.manager = null;
        }
        this.topScore = UNSCORED;
        this.currentPuzzleName = Parser.UNNAMED_PUZZLE_NAME;
        final int epoch = this.puzzleEpoch.incrementAndGet();
        gui.onPuzzleStopped(epoch, "Solving stopped");
    }
}
