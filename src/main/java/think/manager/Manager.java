package think.manager;

import infra.logging.Logger;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import think.domain.model.Puzzle;
import think.solvers.Solver;
import think.solvers.SolverKind;

/**
    Concurrent solver orchestration and lifecycle management.

    {@snippet lang=mermaid :
    stateDiagram
        [*] --> Idle: new Manager(List<SolverKind>)

        Idle --> Idle: stop()
        Idle --> Running: solve(Puzzle)
        Idle --> [*]: close()

        Running --> Idle: stop()
        Running --> Running: solve(Puzzle)
        Running --> [*]: close()
    }
 */
public final class Manager implements AutoCloseable {

    // An implementation with LMAX Disruptor was also sufficient (see commit history). Manager
    // overhead is real when using many RandomSolvers on small maps, but we deprioritize that niche
    // case for ArrayBlockingQueue's ability to avoid having the caller deal with synchronization.
    private static final ThreadFactory HELL = runnable -> {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    };
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
        solverKinds.forEach(kind -> solvers.add(kind.create(this::insertOrBlock, puzzle)));
        solvers.forEach(solver -> executor.execute(solver::run));
    }

    public List<Proposal> consumeNow() {
        requireExecutorAlive();
        final int approximateSize = buffer.size();
        final List<Proposal> proposals = new ArrayList<>(approximateSize);
        buffer.drainTo(proposals);
        return proposals;
    }

    public Stream<Proposal> streamFor(final Duration duration) {
        requireExecutorAlive();
        final long deadline = System.nanoTime() + duration.toNanos();
        final ArrayDeque<Proposal> backlog = new ArrayDeque<>();
        final AbstractSpliterator<Proposal> spliterator = new AbstractSpliterator<Proposal>(
            Long.MAX_VALUE,
            Spliterator.ORDERED | Spliterator.NONNULL
        ) {
            @Override
            public boolean tryAdvance(final Consumer<? super Proposal> action) {
                if (!backlog.isEmpty()) {
                    action.accept(backlog.removeFirst());
                    return true;
                }
                final long remainingTime = deadline - System.nanoTime();
                if (remainingTime < 0) {
                    return false;
                }
                try {
                    final Proposal proposal = buffer.poll(remainingTime, TimeUnit.NANOSECONDS);
                    if (proposal == null) {
                        return false;
                    }
                    buffer.drainTo(backlog);
                    action.accept(proposal);
                    return true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new AssertionError();
                }
            }
        };
        return StreamSupport.stream(spliterator, false);
    }

    public synchronized void stop() {
        requireExecutorAlive();
        final long startTimeNanos = System.nanoTime();
        // Buffer needs to be cleared before awaitTermination so solvers blocked on it can proceed
        // and eventually die. This trick only works if the buffer size has more elements than the
        // number of solver threads, since in the worst case each solver can sneak in 1 (we depend
        // on each solver running on a single thread) element after termination requested.
        solvers.forEach(Solver::requestTermination);
        buffer.clear();
        solvers.forEach(Solver::awaitTermination);
        buffer.clear();
        solvers.clear();
        final long endTimeNanos = System.nanoTime();
        Logger.debug("stop() in %d ms", (endTimeNanos - startTimeNanos) / 1_000_000);
    }

    @Override
    public synchronized void close() {
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
