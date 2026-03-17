package think.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import think.common.StandardEvaluator;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.solvers.Solver;

/**
    Concurrent solver orchestration and lifecycle management.

    Managers are resuable. The callback may come from any thread. At most a small number of
    in-flight proposals will be sent after {@link #stop()}, (this is unavoidable if stopping is
    instant) and all in-flight proposals will be completed.
 */
/*
    "The scheduler does not currently implement time sharing for virtual threads" (JEP 444). We
    support an unbounded number of solvers that may never terminate, so we must use platform
    threads and live with the increased memory costs. If parallelism is really coming from "spawn
    more solvers" so the number of platform threads is large, maybe solvers need revision.

    "The [JVM] shutdown sequence begins when all started non-daemon threads have terminated."
    (java.lang.Thread.setDaemon). Workers that spin longer than we want shouldn't prevent Java from
    shutting down, so we'll make them into daemons. But what about wasting resources working on old
    puzzles? That is a legitimate shortcoming, and future work should fix this using ProcessBuilder
    or running Mayulime in a subprocess since after Thread.stop(void) got deprecated you can't
    forcefully stop threads.

    In this implementation, if a solver works on a puzzle, then solving is stopped, then solving
    starts again on the same puzzle, then the solver submits a solution, the submission will be
    considered valid. This is awkward and can be fixed, but there isn't a need (yet) to fix it.
 */
public final class Manager {

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

    private final Consumer<Proposal> listener;
    private final List<SolverRegistry> registry;
    private final Executor executor;
    private final List<Solver> solvers;
    private volatile Puzzle current;

    public Manager(final Consumer<Proposal> listener, final List<SolverRegistry> registry) {
        this.listener = listener;
        this.registry = new ArrayList<>(registry);
        this.executor = Executors.newCachedThreadPool(task -> {
            final Thread thread = new Thread(task);
            thread.setDaemon(true);
            return thread;
        });
        this.solvers = new ArrayList<>(registry.size());
        this.current = null;
    }

    public void solve(final Puzzle puzzle) {
        stop();
        current = puzzle;
        final SolverFactory factory = new SolverFactory(this::consider, puzzle);
        for (final Solver solver : registry.stream().map(factory::create).toList()) {
            solvers.add(solver);
            // Must use execute instead of submit or any other method because exceptions must be
            // propagated all the way up.
            executor.execute(solver);
        }
    }

    public void stop() {
        // Shutting down the Executor would be nice but it is a newCachedThreadPool so the need
        // isn't so pressing, and not shutting it down means we can reuse it.
        current = null;
        solvers.forEach(Solver::requestTermination);
        solvers.clear();
    }

    private void consider(final String submitter, final Puzzle puzzle, final Feature[] features) {
        // It takes non-trivial (10 ms) time to evaluate gargantuan1-like maps, and we're measuring
        // submission time, so it's best to grab the time before validation and evaluation.
        final long createdAtMs = System.currentTimeMillis();
        if (!puzzle.isValid(features)) {
            throw new IllegalArgumentException();
        }
        final int score = StandardEvaluator.evaluate(puzzle, features);
        final Proposal proposal = new Proposal(submitter, puzzle, features, score, createdAtMs);
        if (puzzle == current) {
            listener.accept(proposal);
        }
    }
}
