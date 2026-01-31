package think;

import app.Main;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javafx.application.Platform;
import think.ana.Snake;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;
import think.stra.BlankSolution;
import think.stra.RandomGuesser;
import think.stra.Strategy;

/**
    Strategy controller. Must be called from JavaFX Application Thread. Workers run in
    background to not block the GUI.
 */
public final class Manager {

    private static final Manager INSTANCE = new Manager();
    private final ExecutorService executor;
    private final ArrayList<Strategy> workers;
    private volatile Problem currentProblem;
    private volatile int topScore;

    private Manager() {
        // "The shutdown sequence begins when all started non-daemon threads have
        // terminated.". Workers that spin longer than we want shouldn't prevent Java
        // from shutting down, so we'll make them into daemons.
        final ThreadFactory factory = task -> {
            final Thread thread = new Thread(task);
            thread.setDaemon(true);
            return thread;
        };
        this.executor = Executors.newCachedThreadPool(factory);
        this.workers = new ArrayList<>();
        this.currentProblem = null;
        this.topScore = 0;
    }

    public static Manager getInstance() {
        return INSTANCE;
    }

    // == Strategy management. =========================================================

    public void solve(final Problem problem) {
        assert Platform.isFxApplicationThread();
        // Once the user uploads a new problem, the old workers are useless. I don't
        // know how to kill them, so we must kindly ask for them to stop. I believe
        // that once they stop, the ExecutorService will throw them out, and that will
        // free up resources.
        currentProblem = problem;
        workers.forEach(worker -> worker.pleaseDie());
        workers.clear();
        topScore = 0;
        addWorker(new BlankSolution(problem));
        addWorker(new RandomGuesser(problem));
    }

    private void addWorker(final Strategy worker) {
        workers.add(worker);
        // Unlike submit, execute will propagate exceptions into the FX Thread. Since
        // we use assertions to catch correctness issues, these exceptions must be seen.
        executor.execute(worker);
    }

    // == Communication. ===============================================================

    public int getTopScore() {
        return topScore;
    }

    public void consider(
        final Strategy submitter,
        final Problem problem,
        final Grid<Feature> solution
    ) {
        assert currentProblem != null;
        // This guard is tripped when the user uploads a new problem but worker threads
        // are still running, so workers propose solutions to the old (stale) problem.
        if (currentProblem != problem) {
            return;
        }
        final Grid<Feature> copy = new Grid<>(solution);
        final int score = Snake.evaluate(problem, copy);
        synchronized (this) {
            if (score > topScore) {
                topScore = score;
                Main.getInstance().receive(submitter, problem, copy, score);
            }
        }
    }
}
