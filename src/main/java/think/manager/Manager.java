package think.manager;

import domain.model.Maze;
import domain.model.Puzzle;
import infra.output.Logging;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import think.common.StandardEvaluator;
import think.solvers.Solver;
import think.solvers.baseline.BaselineSolver;

/**
    Solver orchestration and lifecycle management. Supports concurrency.
 */
public final class Manager {

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
        this.topScore = 0;
    }

    public void solve(final Puzzle puzzle) {
        stop();
        current = puzzle;
        final Consumer<Solver> run = solver -> {
            solvers.add(solver);
            executor.execute(solver);
        };
        run.accept(new BaselineSolver(this::consider, puzzle));
    }

    public void stop() {
        current = null;
        solvers.forEach(Solver::requestTermination);
        solvers.clear();
        topScore = 0;
    }

    private void consider(final String submitter, final Puzzle puzzle, final Maze maze) {
        // This guard is tripped when the user uploads a new problem but old worker threads haven't
        // died and propose solutions to the old (stale) problem. If this guard is tripped
        // excessively, it indicates a solver we asked to die refuses to do so.
        if (current != puzzle) {
            Logging.warning("Guard tripped (1).");
            return;
        }
        final int score = StandardEvaluator.evaluate(puzzle, maze);
        // We also check if the score is better in the synchronized section, but as solvers might
        // give this method lots of garbage, if we can early exit without competing for the lock,
        // it would be nice to. Same trick is used for the puzzle.
        if (score <= topScore) {
            return;
        }
        synchronized (this) {
            if (current != puzzle) {
                Logging.warning("Guard tripped (2).");
                return;
            }
            if (score > topScore) {
                Logging.info(
                    "Score %d -> %d on %s by %s from %s",
                    topScore,
                    score,
                    puzzle.getName(),
                    submitter,
                    Thread.currentThread().getName()
                );
                topScore = score;
                listener.accept(new StatusUpdate(submitter, puzzle, maze, score));
            }
        }
    }
}
