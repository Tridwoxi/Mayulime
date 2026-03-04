package solvers;

import domain.old_model.Board;
import domain.old_model.Display;
import domain.old_model.Puzzle;
import infra.output.Logging;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import solvers.baseline.BaselineSolver;
import solvers.bruteforce.RandomSolver;
import solvers.graph.algs.Evaluate;

public final class Manager {

    @FunctionalInterface
    public interface ImprovedSolution {
        void listen(Display display);
    }

    private final ImprovedSolution listener;
    private final ExecutorService executor;
    private final ArrayList<Solver> solvers;
    private volatile Puzzle current;
    private volatile int topScore;

    public Manager(final ImprovedSolution listener) {
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
        final Consumer<Solver> run = solver -> {
            solvers.add(solver);
            executor.execute(solver);
        };
        stop();
        current = puzzle;
        run.accept(new BaselineSolver(this::consider, puzzle));
        run.accept(new RandomSolver(this::consider, puzzle));
    }

    public void stop() {
        current = null;
        solvers.forEach(Solver::pleaseDie);
        solvers.clear();
        topScore = 0;
    }

    private void consider(final String submitter, final Puzzle puzzle, final Board board) {
        // This guard is tripped when the user uploads a new problem but old worker threads haven't
        // died and propose solutions to the old (stale) problem. If this guard is tripped
        // excessively, it indicates a solver we asked to die refuses to do so.
        if (current != puzzle) {
            Logging.warning("Guard tripped (1).");
            return;
        }
        // Boards are mutable data structures, and Solvers make no promises to not mutate them
        // between the time they send it here and the long time later when the caller tries
        // to read it.
        final Board copy = board.shallowCopy();
        final int score = Evaluate.evaluate(puzzle, copy);
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
            if (!puzzle.isValid(copy)) {
                throw new IllegalArgumentException();
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
                listener.listen(new Display(submitter, puzzle, copy, score));
            }
        }
    }
}
