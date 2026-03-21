package think.manager;

import java.util.List;
import java.util.function.Consumer;
import think.domain.model.Puzzle;
import think.solvers.SolverKind;

/**
    Concurrent solver orchestration and lifecycle management.

    Managers are reuseable. {@link #solve(Puzzle)}, {@link #stop()}, and {@link #close()} must be
    called from the same thread. The listener callback will always come from the same thread and
    never sends stale proposals, but must not call the controls.
 */
public final class Manager implements AutoCloseable {

    private final Consumer<Puzzle> onSolve;
    private final Runnable onStop;
    private final Runnable onClose;

    public Manager(final Consumer<Proposal> listener, final List<SolverKind> solverKinds) {
        if (solverKinds.size() == 1) {
            final SingleManager singleManager = new SingleManager(listener, solverKinds.getFirst());
            this.onSolve = singleManager::solve;
            this.onStop = singleManager::stop;
            this.onClose = singleManager::close;
        } else {
            final MultiManager multiManager = new MultiManager(listener, solverKinds);
            this.onSolve = multiManager::solve;
            this.onStop = multiManager::stop;
            this.onClose = multiManager::close;
        }
    }

    public Manager(final Consumer<Proposal> listener, final SolverKind solverKind) {
        this(listener, List.of(solverKind));
    }

    public void solve(final Puzzle puzzle) {
        onSolve.accept(puzzle);
    }

    public void stop() {
        onStop.run();
    }

    @Override
    public void close() {
        onClose.run();
    }
}
