package think;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import think.ana.Pathfind;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;
import think.stra.Strategy;
import think.stra.StrategyBaseline;
import think.stra.StrategyGuessRandomly;
import think.tools.Logging;

/**
    Strategy controller.

    Spawns workers in background to not block callers.
 */
public final class Manager {

    @FunctionalInterface
    public interface ImprovedSolutionListener {
        void listen(
            String submitter,
            Problem problem,
            Grid<Feature> solution,
            int score
        );
    }

    private final ImprovedSolutionListener improvedSolutionListener;
    private final ExecutorService executor;
    private final ArrayList<Strategy> workers;
    private volatile Problem currentProblem;
    private volatile int topScore;

    public Manager(final ImprovedSolutionListener improvedSolutionListener) {
        this.improvedSolutionListener = improvedSolutionListener;
        // "The shutdown sequence begins when all started non-daemon threads have
        // terminated.". Workers that spin longer than we want shouldn't prevent Java
        // from shutting down, so we'll make them into daemons.
        this.executor = Executors.newCachedThreadPool(task -> {
            final Thread thread = new Thread(task);
            thread.setDaemon(true);
            return thread;
        });
        this.workers = new ArrayList<>();
        this.currentProblem = null;
        this.topScore = 0;
    }

    public void solve(final Problem problem) {
        this.currentProblem = problem;
        cleanupPreviousSolve();
        runStrategy(new StrategyBaseline(this::consider, () -> topScore, problem));
        runStrategy(new StrategyGuessRandomly(this::consider, () -> topScore, problem));
    }

    private void cleanupPreviousSolve() {
        workers.forEach(worker -> worker.pleaseDie());
        workers.clear();
        topScore = 0;
    }

    private void runStrategy(final Strategy worker) {
        workers.add(worker);
        // Unlike submit, execute will propagate exceptions into the FX Thread. Since
        // we use assertions to catch correctness issues, these exceptions must be seen.
        executor.execute(worker);
    }

    private void consider(
        final String submitter,
        final Problem problem,
        final Grid<Feature> solution,
        final int score
    ) {
        assert currentProblem != null;
        // Grids are mutable data structures, and strategies make no promises to not
        // mutate the grid between the time they send it here and the long time later
        // when the caller tries to read it.
        final Grid<Feature> copy = new Grid<>(solution);
        assert score == Pathfind.evaluate(problem, copy);
        synchronized (this) {
            // This guard is tripped when the user uploads a new problem but old worker
            // threads haven't died and propose solutions to the old (stale) problem.
            // If this guard is tripped excessively, it indicates a strategy we asked
            // to die refuses to do so.
            if (currentProblem != problem) {
                Logging.log(getClass(), "Guard tripped.");
                return;
            }
            if (score > topScore) {
                Logging.log(
                    getClass(),
                    "Score %d -> %d on %s by %s from %s",
                    topScore,
                    score,
                    problem.getName(),
                    submitter,
                    Thread.currentThread().getName()
                );
                topScore = score;
                improvedSolutionListener.listen(submitter, problem, copy, score);
            }
        }
    }
}
