package think;

import app.Main;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import think.ana.Snake;
import think.repr.Cell;
import think.repr.Problem;
import think.stra.Blind;
import think.stra.Climb;

/**
    Strategy controller. Must be called from JavaFX Application Thread. Workers run in
    background to not block the GUI.
 */
public final class Manager {

    public interface Strategy extends Runnable {
        String getName();
    }

    private static final Manager INSTANCE = new Manager();

    private volatile ExecutorService tasks;
    private volatile Problem activeProblem;
    private volatile int topScore;

    private Manager() {
        this.tasks = null;
        this.activeProblem = null;
        this.topScore = 0;
    }

    public static Manager getInstance() {
        return INSTANCE;
    }

    // == Strategy management. =========================================================

    public void solve(final Problem problem) {
        assert Platform.isFxApplicationThread();
        final Strategy nobody = new Strategy() {
            @Override
            public void run() {}

            @Override
            public String getName() {
                return "nobody";
            }
        };
        activeProblem = problem;
        Main.getInstance().recieve(nobody, problem, new HashSet<>(0), 0);
        stop();
        topScore = 0;
        go(problem);
    }

    public void stop() {
        if (tasks == null) {
            return;
        }
        tasks.shutdownNow();
        try {
            tasks.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void go(final Problem problem) {
        if (tasks == null || tasks.isShutdown() || tasks.isTerminated()) {
            tasks = newExecutor();
        }
        // Unlike submit, execute will propagate exceptions into the FX Thread. Since
        // we use assertions to catch correctness issues, these exceptions must be seen.
        tasks.execute(new Blind(problem));
        tasks.execute(new Climb(problem));
    }

    private ExecutorService newExecutor() {
        // Making a thread a daemon means when the GUI window is closed, the
        // application is happy to terminate. When these threads are not daemons, the
        // user is forced to keyboard interrupt or kill Java.
        return Executors.newCachedThreadPool(runnable -> {
            final Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
    }

    // == Communication. ===============================================================

    public int getTopScore() {
        return topScore;
    }

    public void consider(
        final Strategy submitter,
        final Problem problem,
        final HashSet<Cell> playerWalls
    ) {
        assert activeProblem != null;
        // This guard is tripped when the user uploads a new problem but worker threads
        // are still running, so workers propose solutions to the old (stale) problem.
        if (activeProblem != problem) {
            return;
        }
        final HashSet<Cell> copy = new HashSet<>(playerWalls);
        assert isValidAssignment(problem, copy);
        final int score = Snake.evaluate(problem, copy);
        synchronized (this) {
            if (score > topScore) {
                topScore = score;
                Main.getInstance().recieve(submitter, problem, copy, score);
            }
        }
    }

    private boolean isValidAssignment(
        final Problem problem,
        final HashSet<Cell> playerWalls
    ) {
        final boolean limited = playerWalls.size() <= problem.getNumPlayerWalls();
        final boolean within = problem.getEmptyCells().containsAll(playerWalls);
        return limited && within;
    }
}
