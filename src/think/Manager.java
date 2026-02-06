package think;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import think.ana.Pathfind;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;
import think.stra.BlankSolution;
import think.stra.RandomGuesser;
import think.stra.Strategy;
import think.tools.Logging;

/**
    Strategy controller.

    Workers run in background to not block the GUI.
 */
public final class Manager {

    /**
        Improvement listener.

        This interface will be called when a solution is considered and verified to be
        an improvement.
     */
    @FunctionalInterface
    public interface Alerter {
        void alert(String submitter, Problem problem, Grid<Feature> solution, int score);
    }

    private final Alerter alerter;
    private final ExecutorService executor;
    private final ArrayList<Strategy> workers;
    private volatile Problem currentProblem;
    private volatile int topScore;

    public Manager(final Alerter alerter) {
        this.alerter = alerter;
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
        currentProblem = problem;
        workers.forEach(worker -> worker.pleaseDie());
        workers.clear();
        topScore = 0;
        runStrategy(new BlankSolution(this::consider, () -> topScore, problem));
        runStrategy(new RandomGuesser(this::consider, () -> topScore, problem));
    }

    private void runStrategy(final Strategy worker) {
        workers.add(worker);
        // Unlike submit, execute will propagate exceptions into the FX Thread. Since
        // we use assertions to catch correctness issues, these exceptions must be seen.
        executor.execute(worker);
    }

    /**
        Relay a solution to the frontend if it is the best so far.
     */
    private void consider(
        final String submitter,
        final Problem problem,
        final Grid<Feature> solution
    ) {
        assert currentProblem != null;
        // Grids are mutable data structures, and strategies make no promises to not
        // mutate the grid between the time they send it here and the long time later
        // when the frontend tries to read it.
        final Grid<Feature> copy = new Grid<>(solution);
        final int score = Pathfind.evaluate(problem, copy);
        synchronized (this) {
            // This guard is tripped when the user uploads a new problem but old worker
            // threads haven't died and propose solutions to the old (stale) problem.
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
                alerter.alert(submitter, problem, copy, score);
            }
        }
    }
}
