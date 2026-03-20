package think.manager;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import think.common.StandardEvaluator;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.solvers.Solver;
import think.solvers.SolverCatalog;

/**
    Concurrent solver orchestration and lifecycle management.

    Bridges are reuseable. {@link #solve(Puzzle)} and {@link #stop()} must be called from the same
    thread. The listener callback will always come from the same thread.
 */
public final class Bridge {

    public record Proposal(
        String submitter,
        Puzzle puzzle,
        Feature[] features,
        int score,
        long createdAtMs
    ) {
        public Proposal {
            features = features.clone();
        }

        public Feature[] features() {
            return features.clone();
        }
    }

    // Small buffer means poor burst tolerance (burst tolerance = buffer size / throughput). Big
    // buffer means less cache. One ought to measure buffer capacity and tune it.
    private static final int BUFFER_SIZE = 4096; // Untuned arbitrary power of 2.
    private final ReadWriteLock lock;
    private final Disruptor<Event> disruptor;
    private final RingBuffer<Event> buffer;
    private final Executor executor;
    private final List<Solver> solvers;
    private final List<SolverKind> solverKinds;
    private Puzzle current;

    public Bridge(final Consumer<Proposal> listener, final List<SolverKind> solverKinds) {
        this.lock = new ReentrantReadWriteLock(true);
        // We don't really need Disruptor for this (an ArrayBlockingQueue will do just fine; input
        // is like 12 million/second for 10 RandomSolvers) but I wanted to use it LOLLLLL!
        this.disruptor = new Disruptor<>(Event::new, BUFFER_SIZE, DaemonThreadFactory.INSTANCE);
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
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void consider(final String submitter, final Puzzle puzzle, final Feature[] features) {
        // It takes non-trivial (10 ms) time to evaluate gargantuan1-like maps, and we're measuring
        // submission time, so it's most honest to grab the time as early as possible.
        final long createdAtMs = System.currentTimeMillis();
        if (!puzzle.isValid(features)) {
            throw new IllegalArgumentException();
        }
        final int score = StandardEvaluator.evaluate(puzzle, features);
        final Proposal proposal = new Proposal(submitter, puzzle, features, score, createdAtMs);
        lock.readLock().lock();
        try {
            // If a solver works on a puzzle, then solving is stopped, then solving starts again on
            // the same puzzle, then the solver submits a puzzle, it will be accepted. This is
            // technically correct when you want solutions, but awkward for benchmarking.
            if (current == puzzle) {
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
