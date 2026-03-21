package think.manager;

import com.lmax.disruptor.util.DaemonThreadFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import think.domain.model.Puzzle;
import think.solvers.Solver;
import think.solvers.SolverCatalog;
import think.solvers.SolverKind;

/**
    This implementation has almost no concurrency overhead.
 */
final class SingleManager {

    private final Consumer<Proposal> listener;
    private final SolverKind solverKind;
    private final ExecutorService executor;
    private final List<Solver> solvers;
    private volatile Puzzle current;
    private volatile int inFlight;

    SingleManager(final Consumer<Proposal> listener, final SolverKind solverKind) {
        this.listener = listener;
        this.solverKind = solverKind;
        this.executor = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);
        this.solvers = new ArrayList<>(1);
        this.current = null;
        this.inFlight = 0;
    }

    void solve(final Puzzle puzzle) {
        current = puzzle;
        solvers.add(new SolverCatalog(this::consider, puzzle).create(solverKind));
        executor.execute(solvers.get(0));
    }

    void stop() {
        current = null;
        solvers.forEach(Solver::requestTermination);
        solvers.clear();
        while (inFlight > 0) {
            LockSupport.parkNanos(1L);
        }
    }

    void close() {
        stop();
        executor.shutdown();
    }

    private void consider(final Proposal proposal) {
        if (current != proposal.getPuzzle()) {
            return;
        }
        inFlight = 1;
        if (current != proposal.getPuzzle()) {
            inFlight = 0;
            return;
        }
        listener.accept(proposal);
        inFlight = 0;
    }
}
