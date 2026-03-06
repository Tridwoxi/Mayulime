package infra.gui;

import java.io.File;
import java.util.List;
import java.util.Locale;
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
    private String pendingSubmittedMapCode;
    private String lastAcceptedMapCode;

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
        this.pendingSubmittedMapCode = null;
        this.lastAcceptedMapCode = null;

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

    public void onPuzzleAccepted(
        final String puzzleName,
        final int rows,
        final int cols,
        final int wallBudget,
        final int puzzleEpoch
    ) {
        runOnFxThread(() -> this.acceptPuzzle(puzzleName, rows, cols, wallBudget, puzzleEpoch));
    }

    public void onPuzzleRejected(final int puzzleEpoch, final String message) {
        runOnFxThread(() -> this.rejectPuzzle(puzzleEpoch, message));
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

    private void acceptPuzzle(
        final String puzzleName,
        final int rows,
        final int cols,
        final int wallBudget,
        final int puzzleEpoch
    ) {
        final long nowNanos = System.nanoTime();
        this.latestPending.set(null);
        if (this.pendingSubmittedMapCode != null) {
            this.lastAcceptedMapCode = this.pendingSubmittedMapCode;
        }
        this.pendingSubmittedMapCode = null;
        this.state = new UiState(
            UiPhase.SOLVING,
            puzzleEpoch,
            puzzleName,
            rows,
            cols,
            wallBudget,
            null,
            0,
            0,
            this.state.cellSizePx(),
            this.lastAcceptedMapCode != null,
            "Solving: searching for better solutions",
            nowNanos,
            -1L,
            -1L,
            true
        );
        this.renderState(nowNanos);
    }

    private void rejectPuzzle(final int puzzleEpoch, final String message) {
        this.latestPending.set(null);
        this.pendingSubmittedMapCode = null;
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
            this.lastAcceptedMapCode != null,
            message,
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
        final String finalStatus = buildStoppedStatusMessage(this.state.bestUpdate(), message);
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
            this.lastAcceptedMapCode != null,
            finalStatus,
            this.state.puzzleStartedAtNanos(),
            this.state.lastUpdateAtNanos(),
            freezeAtNanos,
            false
        );
        this.renderState(freezeAtNanos);
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
                String.format(
                    Locale.US,
                    "Solving: current score %d by %s",
                    update.getScore(),
                    update.getSubmitter()
                ),
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
        this.pendingSubmittedMapCode = mapCode;
        this.mapCodeConsumer.accept(mapCode);
    }

    private void stopOrRestart() {
        if (this.state.phase() == UiPhase.SOLVING) {
            this.stopConsumer.run();
            return;
        }
        if (this.lastAcceptedMapCode != null) {
            this.mapCodeConsumer.accept(this.lastAcceptedMapCode);
        }
    }

    private void requestUploadMapCode() {
        this.handleMapCodeInput(this.mapCodeInputHandler.chooseMapCode(this.rootView.getWindow()));
    }

    private void requestPasteMapCode() {
        this.handleMapCodeInput(this.mapCodeInputHandler.loadPastedMapCode());
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
            message,
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

    private static String buildStoppedStatusMessage(
        final StatusUpdate bestUpdate,
        final String fallback
    ) {
        if (bestUpdate == null) {
            return fallback;
        }
        return String.format(
            Locale.US,
            "Final score: %d by %s",
            bestUpdate.getScore(),
            bestUpdate.getSubmitter()
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
        public void onMapCodeFilesDropped(final List<File> files) {
            UiController.this.handleDroppedFiles(files);
        }
    }
}
