package infra.launch;

import infra.gui.Gui;
import infra.gui.Submission;
import infra.logging.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;
import think.domain.codec.Parser;
import think.domain.codec.Parser.BadMapCodeException;
import think.domain.codec.Serializer;
import think.domain.model.Puzzle;
import think.manager.Manager;
import think.manager.Proposal;
import think.solvers.SolverKind;

/**
    Normal application launch point. Connects Gui (frontend) to Manager (backend).

    Everything runs on the JavaFX application thread. A Timeline drains the manager's proposal
    buffer periodically and pushes at most one render per tick into the Gui. Because no work
    crosses threads, there is no need for atomic state, update coalescing buffers, or epoch
    guards against stale FX events.

    Known limitation: if solvers collectively produce proposals faster than the buffer drains
    per tick, they block on {@code buffer.put}. This is accepted because it does not matter in
    practice — either the puzzle is easy and a bit of throttling still solves it quickly, or
    the puzzle is hard and per-solver throughput is low enough that the buffer never fills.
 */
public final class App extends Application {

    private static final int UNSCORED = -2; // StandardEvaluator uses -1.
    private static final Duration POLL_PERIOD = Duration.millis(33.0);
    private static final String BAD_MAP_MESSAGE =
        "Unable to parse that file as supported Pathery MapCode.";
    private final Timeline pollTicker;
    private int topScore;
    private String currentPuzzleName;
    private Manager manager;
    private Gui gui;

    public App() {
        this.pollTicker = new Timeline(new KeyFrame(POLL_PERIOD, _ -> drainManager()));
        this.pollTicker.setCycleCount(Animation.INDEFINITE);
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

    private void drainManager() {
        if (manager == null) {
            return;
        }
        Proposal best = null;
        for (final Proposal proposal : manager.consumeNow()) {
            if (proposal.getScore() <= this.topScore) {
                continue;
            }
            logScoreImprovement(proposal);
            this.topScore = proposal.getScore();
            best = proposal;
        }
        if (best != null) {
            this.gui.onSolverUpdate(
                new Submission(
                    best.getSubmitter(),
                    best.getPuzzle(),
                    best.getState(),
                    best.getScore()
                )
            );
        }
    }

    private void logScoreImprovement(final Proposal proposal) {
        final String priorScoreText =
            this.topScore == UNSCORED ? "Unscored" : Integer.toString(this.topScore);
        Logger.info(
            "Score %s -> %d on %s by %s",
            priorScoreText,
            proposal.getScore(),
            this.currentPuzzleName,
            proposal.getSubmitter()
        );
        Logger.info(
            "MapCode for score %d: %s",
            proposal.getScore(),
            Serializer.serialize(proposal.getPuzzle(), proposal.getState())
        );
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

        tearDownManager();
        this.topScore = UNSCORED;

        final SolverKind solverKind = gui.getSolverKind();
        final int threadCount = gui.getThreadCount();
        final List<SolverKind> kinds = new ArrayList<>(1 + threadCount);
        kinds.add(SolverKind.BASELINE);
        kinds.addAll(Collections.nCopies(threadCount, solverKind));
        this.manager = new Manager(kinds);

        this.currentPuzzleName = puzzle.name();
        gui.onPuzzleAccepted(puzzle);
        manager.solve(puzzle);
        this.pollTicker.play();
    }

    private void stopRequestedByUser() {
        tearDownManager();
        this.topScore = UNSCORED;
        this.currentPuzzleName = Parser.UNNAMED_PUZZLE_NAME;
        gui.onPuzzleStopped("Solving stopped");
    }

    private void tearDownManager() {
        if (manager == null) {
            return;
        }
        this.pollTicker.stop();
        manager.close();
        this.manager = null;
    }
}
