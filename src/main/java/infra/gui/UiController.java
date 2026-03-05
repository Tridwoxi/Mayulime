package infra.gui;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.util.Duration;
import think.manager.StatusUpdate;

@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
final class UiController {

    private record PendingUpdate(StatusUpdate update, int epoch) {}

    private static final Duration UI_TICK = Duration.millis(33.0);

    private final RootView rootView;
    private final UiRenderer renderer;
    private final Consumer<String> mapCodeConsumer;
    private final Runnable stopConsumer;
    private final Timeline ticker;
    private final AtomicReference<PendingUpdate> latestPending;

    private UiState state;
    private String pendingSubmittedMapCode;
    private String lastAcceptedMapCode;

    UiController(final Consumer<String> mapCodeConsumer, final Runnable stopConsumer) {
        this.rootView = new RootView();
        this.renderer = new UiRenderer(this.rootView);
        this.mapCodeConsumer = mapCodeConsumer;
        this.stopConsumer = stopConsumer;
        this.latestPending = new AtomicReference<>(null);
        this.state = UiState.initial();
        this.pendingSubmittedMapCode = null;
        this.lastAcceptedMapCode = null;

        this.rootView.bindIntents(new IntentsBridge());

        this.ticker = new Timeline(new KeyFrame(UI_TICK, ignored -> this.onFrame()));
        this.ticker.setCycleCount(Animation.INDEFINITE);
        this.ticker.play();

        this.state = this.renderer.render(this.state, System.nanoTime());
    }

    public Parent getRoot() {
        return this.rootView.getRoot();
    }

    public void onPuzzleAccepted(
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
            this.state.cellSizePx(),
            this.lastAcceptedMapCode != null,
            "Solving: searching for better solutions",
            nowNanos,
            -1L,
            -1L,
            true
        );
        this.state = this.renderer.render(this.state, nowNanos);
    }

    public void onPuzzleRejected(final int puzzleEpoch, final String message) {
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
            this.state.cellSizePx(),
            this.lastAcceptedMapCode != null,
            message,
            -1L,
            -1L,
            -1L,
            false
        );
        this.state = this.renderer.render(this.state, System.nanoTime());
    }

    public void onPuzzleStopped(final int puzzleEpoch, final String message) {
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
            this.state.updateCount(),
            this.state.cellSizePx(),
            this.lastAcceptedMapCode != null,
            finalStatus,
            this.state.puzzleStartedAtNanos(),
            this.state.lastUpdateAtNanos(),
            freezeAtNanos,
            false
        );
        this.state = this.renderer.render(this.state, freezeAtNanos);
    }

    public void enqueueSolverUpdate(final StatusUpdate update, final int puzzleEpoch) {
        this.latestPending.set(new PendingUpdate(update, puzzleEpoch));
    }

    private void onFrame() {
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
        }
        this.state = this.renderer.render(this.state, System.nanoTime());
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

    private void reportMapInputError(final String message) {
        this.state = new UiState(
            this.state.phase(),
            this.state.puzzleEpoch(),
            this.state.puzzleName(),
            this.state.rows(),
            this.state.cols(),
            this.state.wallBudget(),
            this.state.bestUpdate(),
            this.state.updateCount(),
            this.state.cellSizePx(),
            this.state.canRestart(),
            message,
            this.state.puzzleStartedAtNanos(),
            this.state.lastUpdateAtNanos(),
            this.state.timersFrozenAtNanos(),
            this.state.recenterPending()
        );
        this.state = this.renderer.render(this.state, System.nanoTime());
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
        public void submitMapCode(final String mapCode) {
            UiController.this.submitMapCode(mapCode);
        }

        @Override
        public void stopOrRestart() {
            UiController.this.stopOrRestart();
        }

        @Override
        public void reportMapInputError(final String message) {
            UiController.this.reportMapInputError(message);
        }
    }
}
