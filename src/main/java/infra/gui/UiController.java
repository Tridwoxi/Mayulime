package infra.gui;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.ColorScheme;
import javafx.scene.Parent;
import javafx.util.Duration;
import think.domain.codec.Serializer;
import think.domain.model.Puzzle;
import think.solvers.SolverKind;

final class UiController {

    private static final Duration TIMER_TICK = Duration.seconds(1.0);

    private final RootView rootView;
    private final UiRenderer renderer;
    private final InputHandler mapCodeInputHandler;
    private final Consumer<String> mapCodeConsumer;
    private final Runnable stopConsumer;
    private final Timeline timerTicker;

    private UiState state;
    private Optional<Puzzle> currentPuzzle;
    private Optional<String> pendingSubmittedMapCode;
    private Optional<String> lastAcceptedMapCode;

    UiController(final Consumer<String> mapCodeConsumer, final Runnable stopConsumer) {
        this.rootView = new RootView();
        this.renderer = new UiRenderer(this.rootView);
        this.mapCodeInputHandler = new InputHandler();
        this.mapCodeConsumer = mapCodeConsumer;
        this.stopConsumer = stopConsumer;
        this.timerTicker = new Timeline(new KeyFrame(TIMER_TICK, _ -> this.refreshTimers()));
        this.state = UiState.initial();
        this.currentPuzzle = Optional.empty();
        this.pendingSubmittedMapCode = Optional.empty();
        this.lastAcceptedMapCode = Optional.empty();

        this.rootView.bindIntents(new IntentsBridge());
        this.rootView.onViewportChanged(this::onViewportChanged);

        this.timerTicker.setCycleCount(Animation.INDEFINITE);
        this.renderState(System.nanoTime());
    }

    public Parent getRoot() {
        return this.rootView.getRoot();
    }

    public SolverKind getSolverKind() {
        return this.rootView.getSolverKind();
    }

    public int getThreadCount() {
        return this.rootView.getThreadCount();
    }

    public void applyColorScheme(final ColorScheme colorScheme) {
        this.rootView.applyPalette(UiPalette.fromColorScheme(colorScheme));
        this.renderState(System.nanoTime());
    }

    public void onPuzzleAccepted(final Puzzle puzzle) {
        final long nowNanos = System.nanoTime();
        final Optional<String> previousMapCode = this.lastAcceptedMapCode;
        this.lastAcceptedMapCode = this.pendingSubmittedMapCode.or(() -> this.lastAcceptedMapCode);
        this.pendingSubmittedMapCode = Optional.empty();
        this.currentPuzzle = Optional.of(puzzle);
        final boolean isRestart =
            previousMapCode.isPresent() && previousMapCode.equals(this.lastAcceptedMapCode);
        this.state = new UiState(
            UiPhase.SOLVING,
            puzzle.name(),
            puzzle.numRows(),
            puzzle.numCols(),
            puzzle.blockingBudget(),
            isRestart ? this.state.bestUpdate() : null,
            isRestart ? this.state.spentWalls() : 0,
            0,
            this.state.cellSizePx(),
            this.lastAcceptedMapCode.isPresent(),
            true,
            "Solving in progress",
            isRestart ? this.state.bestScoreText() : "-",
            isRestart ? this.state.submitterText() : "-",
            nowNanos,
            -1L,
            -1L,
            !isRestart
        );
        this.renderState(nowNanos);
    }

    public void onMapCodeRejected(final String message) {
        this.pendingSubmittedMapCode = Optional.empty();
        this.reportStatusMessage(message);
    }

    public void onPuzzleStopped(final String message) {
        final long freezeAtNanos = System.nanoTime();
        this.state = new UiState(
            UiPhase.STOPPED,
            this.state.puzzleName(),
            this.state.rows(),
            this.state.cols(),
            this.state.wallBudget(),
            this.state.bestUpdate(),
            this.state.spentWalls(),
            this.state.updateCount(),
            this.state.cellSizePx(),
            this.lastAcceptedMapCode.isPresent(),
            this.lastAcceptedMapCode.isPresent(),
            message,
            this.state.bestScoreText(),
            this.state.submitterText(),
            this.state.puzzleStartedAtNanos(),
            this.state.lastUpdateAtNanos(),
            freezeAtNanos,
            false
        );
        this.renderState(freezeAtNanos);
    }

    public void onSolverUpdate(final Submission update) {
        if (this.state.phase() != UiPhase.SOLVING) {
            return;
        }
        final long nowNanos = System.nanoTime();
        this.state = new UiState(
            this.state.phase(),
            this.state.puzzleName(),
            update.getNumRows(),
            update.getNumCols(),
            update.getBlockingBudget(),
            update,
            UiMath.countPlayerWalls(update),
            this.state.updateCount() + 1,
            this.state.cellSizePx(),
            this.state.canRestart(),
            this.state.canCopyMapCode(),
            "Solving in progress",
            String.valueOf(update.getScore()),
            update.getSubmitter(),
            this.state.puzzleStartedAtNanos(),
            nowNanos,
            this.state.timersFrozenAtNanos(),
            this.state.recenterPending()
        );
        this.renderState(nowNanos);
    }

    private void submitMapCode(final String mapCode) {
        this.pendingSubmittedMapCode = Optional.of(mapCode);
        this.mapCodeConsumer.accept(mapCode);
    }

    private void stopOrRestart() {
        if (this.state.phase() == UiPhase.SOLVING) {
            this.stopConsumer.run();
            return;
        }
        this.lastAcceptedMapCode.ifPresent(this::submitMapCode);
    }

    private void requestUploadMapCode() {
        this.handleMapCodeInput(this.mapCodeInputHandler.chooseMapCode(this.rootView.getWindow()));
    }

    private void requestPasteMapCode() {
        this.handleMapCodeInput(this.mapCodeInputHandler.loadPastedMapCode());
    }

    private void requestCopyMapCode() {
        final Optional<String> mapCode = this.currentMapCode();
        if (mapCode.isEmpty()) {
            this.reportStatusMessage("No puzzle is loaded.");
            return;
        }

        final boolean copied = this.mapCodeInputHandler.copyMapCode(mapCode.orElseThrow());
        this.reportStatusMessage(
            copied ? "Copied current maze state as MapCode." : "Couldn't copy MapCode to clipboard."
        );
    }

    private void handleDroppedFiles(final List<File> files) {
        this.handleMapCodeInput(this.mapCodeInputHandler.loadDroppedFiles(files));
    }

    private void handleMapCodeInput(final InputHandler.Result result) {
        switch (result) {
            case InputHandler.AcceptedMapCode accepted -> this.submitMapCode(accepted.mapCode());
            case InputHandler.RejectedMapCode rejected -> this.reportStatusMessage(
                rejected.message()
            );
        }
    }

    private void reportStatusMessage(final String message) {
        this.state = this.state.withStatusMessage(message);
        this.renderState(System.nanoTime());
    }

    private void onViewportChanged() {
        this.renderState(System.nanoTime());
    }

    private void refreshTimers() {
        this.renderState(System.nanoTime());
    }

    private void renderState(final long nowNanos) {
        this.state = this.renderer.render(this.state, nowNanos);
        this.syncTimerTicker();
    }

    private void syncTimerTicker() {
        if (this.state.phase() == UiPhase.SOLVING) {
            if (this.timerTicker.getStatus() != Animation.Status.RUNNING) {
                this.timerTicker.play();
            }
            return;
        }
        this.timerTicker.stop();
    }

    private Optional<String> currentMapCode() {
        if (this.state.bestUpdate() == null) {
            return this.currentPuzzle.map(Serializer::serialize);
        }
        return this.currentPuzzle.map(puzzle ->
            Serializer.serialize(puzzle, this.state.bestUpdate().getState())
        );
    }

    private final class IntentsBridge implements RootView.Intents {

        @Override
        public void onStopOrRestartRequested() {
            UiController.this.stopOrRestart();
        }

        @Override
        public void onUploadMapCodeRequested() {
            UiController.this.requestUploadMapCode();
        }

        @Override
        public void onPasteMapCodeRequested() {
            UiController.this.requestPasteMapCode();
        }

        @Override
        public void onCopyMapCodeRequested() {
            UiController.this.requestCopyMapCode();
        }

        @Override
        public void onMapCodeFilesDropped(final List<File> files) {
            UiController.this.handleDroppedFiles(files);
        }
    }
}
