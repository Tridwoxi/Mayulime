package think;

import infra.io.Logging;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import think.ana.Snake;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;
import think.solve.BaselineSolver;
import think.solve.RandomSolver;
import think.solve.Solver;

/**
    Solver controller.

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

    private final ImprovedSolutionListener listener;
    private final ExecutorService executor;
    private final ArrayList<Solver> workers;
    private volatile Problem currentProblem;
    private volatile int topScore;

    public Manager(final ImprovedSolutionListener listener) {
        this.listener = listener;
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
        runSolver(new BaselineSolver(this::considerSolution, problem));
        runSolver(new RandomSolver(this::considerSolution, problem));
    }

    private void cleanupPreviousSolve() {
        workers.forEach(worker -> worker.pleaseDie());
        workers.clear();
        topScore = 0;
    }

    private void runSolver(final Solver worker) {
        workers.add(worker);
        // Unlike submit, execute will propagate exceptions into the FX Thread. Since
        // we use assertions to catch correctness issues, these exceptions must be seen.
        executor.execute(worker);
    }

    private void considerSolution(
        final String submitter,
        final Problem problem,
        final Grid<Feature> solution,
        final int score
    ) {
        assert currentProblem != null;
        // We also check if the score is better in the synchronized section, but as
        // strategies might give this method lots of garbage, if we can early exit
        // without competing for the lock it would be nice to.
        if (score <= topScore) {
            return;
        }
        // Grids are mutable data structures, and strategies make no promises to not
        // mutate the grid between the time they send it here and the long time later
        // when the caller tries to read it.
        final Grid<Feature> copy = new Grid<>(solution);
        assert score == Snake.evaluate(problem, copy);
        synchronized (this) {
            // This guard is tripped when the user uploads a new problem but old worker
            // threads haven't died and propose solutions to the old (stale) problem.
            // If this guard is tripped excessively, it indicates a solver we asked
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
                listener.listen(submitter, problem, copy, score);
            }
        }
    }
}
