package think.manager;

import infra.output.Logging;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import think.common.StandardEvaluator;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.solvers.Solver;
import think.solvers.baseline.BaselineSolver;
import think.solvers.local.ClimbV1Solver;
import think.solvers.random.RandomSolver;

/**
    Solver orchestration and lifecycle management. Supports concurrency.
 */
public final class Manager {

    private static final int UNSCORED = Integer.MIN_VALUE;
    private final Consumer<StatusUpdate> listener;
    private final Executor executor;
    private final List<Solver> solvers;
    private volatile Puzzle current;
    private volatile int topScore;

    public Manager(final Consumer<StatusUpdate> listener) {
        this.listener = listener;
        // "The shutdown sequence begins when all started non-daemon threads have terminated.".
        // Workers that spin longer than we want shouldn't prevent Java from shutting down, so
        // we'll make them into daemons.
        this.executor = Executors.newCachedThreadPool(task -> {
            final Thread thread = new Thread(task);
            thread.setDaemon(true);
            return thread;
        });
        this.solvers = new ArrayList<>();
        this.current = null;
        this.topScore = UNSCORED;
    }

    public void solve(final Puzzle puzzle) {
        stop();
        current = puzzle;
        final Consumer<Solver> run = solver -> {
            solvers.add(solver);
            executor.execute(solver);
        };
        run.accept(new BaselineSolver(this::consider, puzzle));
        run.accept(new RandomSolver(this::consider, puzzle));
        run.accept(new ClimbV1Solver(this::consider, puzzle));
    }

    public void stop() {
        current = null;
        solvers.forEach(Solver::requestTermination);
        solvers.clear();
        topScore = UNSCORED;
    }

    private void consider(final String submitter, final Puzzle puzzle, final Feature[] features) {
        // This guard is tripped when the user uploads a new problem but old worker threads haven't
        // died and propose solutions to the old (stale) problem. If this guard is tripped
        // excessively, it indicates a solver we asked to die refuses to do so.
        if (current != puzzle) {
            Logging.warning("Guard tripped (1).");
            return;
        }
        if (!puzzle.isValid(features)) {
            throw new IllegalArgumentException();
        }
        final int score = StandardEvaluator.evaluate(puzzle, features);
        if (score == UNSCORED) {
            throw new IllegalStateException();
        }
        // We also check if the score is better in the synchronized section, but as solvers might
        // give this method lots of garbage, if we can early exit without competing for the lock,
        // it would be nice to. Same trick is used for the puzzle.
        if (score <= topScore && topScore != UNSCORED) {
            return;
        }
        synchronized (this) {
            if (current != puzzle) {
                Logging.warning("Guard tripped (2).");
                return;
            }
            if (score > topScore || topScore == UNSCORED) {
                Logging.info(
                    "Score %d -> %d on %s by %s from %s",
                    topScore,
                    score,
                    puzzle.getName(),
                    submitter,
                    Thread.currentThread().getName()
                );
                topScore = score;
                listener.accept(new StatusUpdate(submitter, puzzle, features, score));
            }
        }
    }
}
