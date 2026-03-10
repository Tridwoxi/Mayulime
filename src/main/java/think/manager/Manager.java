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
    Solver orchestration and lifecycle management. Supports concurrency.
 */
public final class Manager {

    private final Consumer<Submission> listener;
    private final List<SolverRegistry> registry;
    private final Executor executor;
    private final List<Solver> solvers;
    private volatile Puzzle current;

    public Manager(final Consumer<Submission> listener, final List<SolverRegistry> registry) {
        this.listener = listener;
        this.registry = new ArrayList<>(registry);
        // "The shutdown sequence begins when all started non-daemon threads have terminated.".
        // Workers that spin longer than we want shouldn't prevent Java from shutting down, so
        // we'll make them into daemons.
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
            executor.execute(solver);
        }
    }

    public void stop() {
        current = null;
        solvers.forEach(Solver::requestTermination);
        solvers.clear();
    }

    private void consider(final String submitter, final Puzzle puzzle, final Feature[] features) {
        final Feature[] copy = features.clone();
        if (!puzzle.isValid(copy)) {
            throw new IllegalArgumentException();
        }
        final int score = StandardEvaluator.evaluate(puzzle, copy);
        synchronized (this) {
            // This guard is tripped when the user uploads a new problem but old worker threads
            // haven't died and propose solutions to the old (stale) problem.
            if (puzzle != current) {
                return;
            }
            listener.accept(new Submission(submitter, puzzle, copy, score));
        }
    }
}
