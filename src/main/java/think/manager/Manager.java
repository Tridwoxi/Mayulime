package think.manager;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import think.domain.model.Puzzle;
import think.solvers.Solver;
import think.solvers.SolverCatalog;

/**
    Concurrent solver orchestration and lifecycle management.

    Managers are reuseable. {@link #solve(Puzzle)}, {@link #stop()}, and {@link #close()} must be
    called from the same thread. The listener callback will always come from the same thread and
    never sends stale proposals, but must not call the controls.

    On my machine (Apple M4 Pro chip) for small1, this implementation can handle ~2.5M proposals
    /second, but a single RandomSolver can supply ~1.6M proposals/second, so solvers should either
    only propose improvements or guess slower.
 */
public final class Manager implements AutoCloseable {

    // Small buffer means poor burst tolerance (burst tolerance = buffer size / throughput). Big
    // buffer means less cache. One ought to measure buffer capacity and tune it.
    private static final int BUFFER_SIZE = 1024;
    private final ReadWriteLock lock;
    private final Disruptor<Event> disruptor;
    private final RingBuffer<Event> buffer;
    private final ExecutorService executor;
    private final List<Solver> solvers;
    private final List<SolverKind> solverKinds;
    private Puzzle current;

    public Manager(final Consumer<Proposal> listener, final List<SolverKind> solverKinds) {
        this.lock = new ReentrantReadWriteLock(true);
        this.disruptor = new Disruptor<>(
            Event::new,
            BUFFER_SIZE,
            DaemonThreadFactory.INSTANCE,
            solverKinds.size() <= 1 ? ProducerType.SINGLE : ProducerType.MULTI,
            new BlockingWaitStrategy() // Fastest for 10 RandomSolvers.
        );
        disruptor.handleEventsWith((event, _, _) -> listener.accept(event.proposal));
        this.buffer = disruptor.start();
        this.executor = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);
        this.solvers = new ArrayList<>(solverKinds.size());
        this.solverKinds = new ArrayList<>(solverKinds);
        this.current = null;
    }

    public void solve(final Puzzle puzzle) {
        stop();
        this.current = puzzle;
        final SolverCatalog catalog = new SolverCatalog(this::consider, puzzle);
        for (final Solver solver : solverKinds.stream().map(catalog::create).toList()) {
            solvers.add(solver);
            // Must use execute instead of submit or any other method because exceptions must be
            // propagated all the way up.
            executor.execute(solver);
        }
    }

    public void stop() {
        lock.writeLock().lock();
        try {
            this.current = null;
            // Solvers may terminate after unbounded time (because user controls input size) so
            // Process.destroyForcibly is the right approach, but it requires serialization, is
            // really slow, and the current guards are probably good enough.
            solvers.forEach(Solver::requestTermination);
            solvers.clear();
            // This condition works for draining because in Disruptor 4.0.0, remaining capacity is
            // only changed after an event has been fully consumed.
            while (buffer.remainingCapacity() < buffer.getBufferSize()) {
                LockSupport.parkNanos(1L);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void close() {
        stop();
        disruptor.shutdown();
        executor.shutdown();
    }

    private void consider(final Proposal proposal) {
        lock.readLock().lock();
        try {
            // If a solver works on a puzzle, then solving is stopped, then solving starts again on
            // the same puzzle, then the solver submits a puzzle, it will be accepted. This is
            // technically correct when you want solutions, albeit awkward for benchmarking.
            if (current == proposal.getPuzzle()) {
                buffer.publishEvent((event, sequence) -> event.proposal = proposal);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private static final class Event {

        private Proposal proposal;
    }
}
