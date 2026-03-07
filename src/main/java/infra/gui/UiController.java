package infra.gui;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.util.Duration;
import think.domain.codec.Serializer;
import think.domain.model.Puzzle;
import think.manager.StatusUpdate;

@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
final class UiController {

    private record PendingUpdate(StatusUpdate update, int epoch) {}

    private static final Duration TIMER_TICK = Duration.seconds(1.0);

    private final RootView rootView;
    private final UiRenderer renderer;
    private final InputHandler mapCodeInputHandler;
    private final Consumer<String> mapCodeConsumer;
    private final Runnable stopConsumer;
    private final Timeline timerTicker;
    private final AtomicReference<PendingUpdate> latestPending;
    private final AtomicBoolean updateFlushScheduled;

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
        this.timerTicker = new Timeline(new KeyFrame(TIMER_TICK, ignored -> this.refreshTimers()));
        this.latestPending = new AtomicReference<>(null);
        this.updateFlushScheduled = new AtomicBoolean(false);
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

    public void applyColorScheme(final ColorScheme colorScheme) {
        runOnFxThread(() -> {
            this.rootView.applyPalette(UiPalette.fromColorScheme(colorScheme));
            this.renderState(System.nanoTime());
        });
    }

    public void onPuzzleAccepted(final Puzzle puzzle, final int puzzleEpoch) {
        runOnFxThread(() -> this.acceptPuzzle(puzzle, puzzleEpoch));
    }

    public void onPuzzleRejected(final int puzzleEpoch, final String message) {
        runOnFxThread(() -> this.rejectPuzzle(puzzleEpoch, message));
    }

    public void onMapCodeRejected(final String message) {
        runOnFxThread(() -> this.rejectMapCode(message));
    }

    public void onPuzzleStopped(final int puzzleEpoch, final String message) {
        runOnFxThread(() -> this.stopPuzzle(puzzleEpoch, message));
    }

    public void enqueueSolverUpdate(final StatusUpdate update, final int puzzleEpoch) {
        this.latestPending.set(new PendingUpdate(update, puzzleEpoch));
        if (this.updateFlushScheduled.compareAndSet(false, true)) {
            Platform.runLater(this::flushPendingUpdate);
        }
    }

    private void acceptPuzzle(final Puzzle puzzle, final int puzzleEpoch) {
        final long nowNanos = System.nanoTime();
        this.latestPending.set(null);
        this.lastAcceptedMapCode = this.pendingSubmittedMapCode.or(() -> this.lastAcceptedMapCode);
        this.pendingSubmittedMapCode = Optional.empty();
        this.currentPuzzle = Optional.of(puzzle);
        this.state = new UiState(
            UiPhase.SOLVING,
            puzzleEpoch,
            puzzle.getName(),
            puzzle.getNumRows(),
            puzzle.getNumCols(),
            puzzle.getBlockingBudget(),
            null,
            0,
            0,
            this.state.cellSizePx(),
            this.lastAcceptedMapCode.isPresent(),
            true,
            "Solving in progress",
            "-",
            "-",
            nowNanos,
            -1L,
            -1L,
            true
        );
        this.renderState(nowNanos);
    }

    private void rejectPuzzle(final int puzzleEpoch, final String message) {
        this.latestPending.set(null);
        this.pendingSubmittedMapCode = Optional.empty();
        this.currentPuzzle = Optional.empty();
        this.state = new UiState(
            UiPhase.REJECTED,
            puzzleEpoch,
            "Rejected puzzle",
            0,
            0,
            0,
            null,
            0,
            0,
            this.state.cellSizePx(),
            this.lastAcceptedMapCode.isPresent(),
            false,
            message,
            "-",
            "-",
            -1L,
            -1L,
            -1L,
            false
        );
        this.renderState(System.nanoTime());
    }

    private void stopPuzzle(final int puzzleEpoch, final String message) {
        this.latestPending.set(null);
        final long freezeAtNanos = System.nanoTime();
        this.state = new UiState(
            UiPhase.STOPPED,
            puzzleEpoch,
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

    private void rejectMapCode(final String message) {
        this.pendingSubmittedMapCode = Optional.empty();
        this.reportMapInputError(message);
    }

    private void flushPendingUpdate() {
        this.updateFlushScheduled.set(false);
        final PendingUpdate pending = this.latestPending.getAndSet(null);
        if (
            pending != null &&
            pending.epoch() == this.state.puzzleEpoch() &&
            this.state.phase() == UiPhase.SOLVING
        ) {
            final StatusUpdate update = pending.update();
            final long nowNanos = System.nanoTime();
            this.state = new UiState(
                this.state.phase(),
                this.state.puzzleEpoch(),
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

        if (
            this.latestPending.get() != null && this.updateFlushScheduled.compareAndSet(false, true)
        ) {
            Platform.runLater(this::flushPendingUpdate);
        }
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
            this.reportMapInputError("No puzzle is loaded.");
            return;
        }

        final String message = this.mapCodeInputHandler.copyMapCode(mapCode.orElseThrow())
            ? "Copied current board state as MapCode."
            : "Couldn't copy MapCode to clipboard.";
        this.reportMapInputError(message);
    }

    private void handleDroppedFiles(final List<File> files) {
        this.handleMapCodeInput(this.mapCodeInputHandler.loadDroppedFiles(files));
    }

    private void handleMapCodeInput(final InputHandler.Result result) {
        if (result instanceof InputHandler.AcceptedMapCode acceptedMapCode) {
            this.submitMapCode(acceptedMapCode.mapCode());
            return;
        }
        final InputHandler.RejectedMapCode rejectedMapCode = (InputHandler.RejectedMapCode) result;
        this.reportMapInputError(rejectedMapCode.message());
    }

    private void reportMapInputError(final String message) {
        this.state = new UiState(
            this.state.phase(),
            this.state.puzzleEpoch(),
            this.state.puzzleName(),
            this.state.rows(),
            this.state.cols(),
            this.state.wallBudget(),
            this.state.bestUpdate(),
            this.state.spentWalls(),
            this.state.updateCount(),
            this.state.cellSizePx(),
            this.state.canRestart(),
            this.state.canCopyMapCode(),
            message,
            this.state.bestScoreText(),
            this.state.submitterText(),
            this.state.puzzleStartedAtNanos(),
            this.state.lastUpdateAtNanos(),
            this.state.timersFrozenAtNanos(),
            this.state.recenterPending()
        );
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

    private static void runOnFxThread(final Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        Platform.runLater(action);
    }

    private Optional<String> currentMapCode() {
        if (this.state.bestUpdate() == null) {
            return this.currentPuzzle.map(Serializer::serialize);
        }
        return this.currentPuzzle.map(puzzle ->
            Serializer.serialize(puzzle, this.state.bestUpdate().getFeatures())
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
