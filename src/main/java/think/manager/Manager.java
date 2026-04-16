package think.manager;

import infra.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import think.domain.model.Puzzle;
import think.solvers.Solver;
import think.solvers.SolverKind;

/**
    Concurrent solver orchestration and lifecycle management.
 */
public final class Manager implements AutoCloseable {

    private static final ThreadFactory HELL = runnable -> {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    };
    private static final int CONSUME_POLL_INTERVAL_NANOS = 100_000;
    private static final int MIN_BUFFER_SIZE = 1000;
    private final int bufferCapacity;
    private final List<SolverKind> solverKinds;
    private final ArrayBlockingQueue<Proposal> buffer;
    private final ExecutorService executor;
    private final List<Solver> solvers;

    public Manager(final List<SolverKind> solverKinds) {
        this.bufferCapacity = Math.max(MIN_BUFFER_SIZE, 2 * solverKinds.size() + 10);
        this.solverKinds = new ArrayList<>(solverKinds);
        this.buffer = new ArrayBlockingQueue<>(bufferCapacity, true);
        this.executor = Executors.newFixedThreadPool(solverKinds.size(), HELL);
        this.solvers = new ArrayList<>(solverKinds.size());
    }

    public synchronized void solve(final Puzzle puzzle) {
        stop();
        buffer.clear();
        solverKinds.forEach(kind -> solvers.add(kind.create(this::insertOrBlock, puzzle)));
        solvers.forEach(solver -> executor.execute(solver::run));
    }

    public List<Proposal> consumeNow() {
        requireExecutorAlive();
        final List<Proposal> proposals = new ArrayList<>(buffer.size());
        buffer.drainTo(proposals);
        return proposals;
    }

    public List<Proposal> consumeUntil(final long timeoutMillis) {
        requireExecutorAlive();
        final long endTimeNanos = System.nanoTime() + timeoutMillis * 1_000_000;
        final List<Proposal> proposals = new ArrayList<>();
        while (System.nanoTime() < endTimeNanos) {
            final int numDrained = buffer.drainTo(proposals);
            if (numDrained == 0) {
                try {
                    Thread.sleep(0L, CONSUME_POLL_INTERVAL_NANOS);
                } catch (InterruptedException exception) {
                    throw new AssertionError(exception);
                }
            }
        }
        return proposals;
    }

    public synchronized void stop() {
        requireExecutorAlive();
        final long startTimeNanos = System.nanoTime();
        // Buffer needs to be cleared before awaiting termination so solvers blocked on it can
        // continue and eventually die. This trick only works if the buffer size has more elements
        // than the number of solvers, since in the worst case each solver can sneak in 1 element.
        solvers.forEach(Solver::requestTermination);
        buffer.clear();
        solvers.forEach(Solver::awaitTermination);
        solvers.clear();
        final long endTimeNanos = System.nanoTime();
        Logger.debug("stop() in %d millis", (endTimeNanos - startTimeNanos) / 1_000_000);
    }

    @Override
    public void close() {
        stop();
        executor.shutdown();
    }

    private void insertOrBlock(final Proposal proposal) {
        final int bufferSize = buffer.size();
        if (bufferSize > bufferCapacity / 2) {
            Logger.warning("buffer past half capacity with %d elements", bufferSize);
            Logger.warning("today, we blame %s for our woes", proposal.getSubmitter());
        }
        try {
            buffer.put(proposal);
        } catch (InterruptedException exception) {
            throw new AssertionError(exception);
        }
    }

    private void requireExecutorAlive() {
        if (executor.isShutdown()) {
            throw new IllegalStateException();
        }
    }
}
