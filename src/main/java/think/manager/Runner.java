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

/**
    Optimized version of {@link Manager} for a single producer and single lightweight consumer.
    About 1.5x faster (1.2M per second versus 800k per second) for RandomSolver on small1.
 */
public final class Runner implements AutoCloseable {

    private final Consumer<Proposal> listener;
    private final SolverKind solverKind;
    private final ExecutorService executor;
    private final List<Solver> solvers;
    private volatile Puzzle current;
    private volatile int inFlight;

    public Runner(final Consumer<Proposal> listener, final SolverKind solverKind) {
        this.listener = listener;
        this.solverKind = solverKind;
        this.executor = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);
        this.solvers = new ArrayList<>(1);
        this.current = null;
        this.inFlight = 0;
    }

    public void solve(final Puzzle puzzle) {
        current = puzzle;
        solvers.add(new SolverCatalog(this::consider, puzzle).create(solverKind));
        executor.execute(solvers.get(0));
    }

    public void stop() {
        current = null;
        solvers.forEach(Solver::requestTermination);
        solvers.clear();
        while (inFlight > 0) {
            LockSupport.parkNanos(1L);
        }
    }

    @Override
    public void close() {
        stop();
        executor.shutdown();
    }

    private void consider(final Proposal proposal) {
        if (current != proposal.getPuzzle()) {
            return;
        }
        // Optimization: single producer enables LongAdder begone.
        inFlight = 1;
        if (current != proposal.getPuzzle()) {
            inFlight = 0;
            return;
        }
        // Optimization: lightweight listener enables consumption directly from producer thread,
        // but perhaps more importantly, no Disruptor.
        listener.accept(proposal);
        inFlight = 0;
    }
}
