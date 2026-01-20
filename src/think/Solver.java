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
    Strategy manager and bridge from backend to GUI. Must be called from JavaFX
    Application Thread. Workers run in background to not block the GUI.
 */
public final class Solver {

    private static volatile ExecutorService tasks = null;
    private static volatile Problem activeProblem = null;
    private static volatile int topScore = 0;

    private Solver() {}

    // == Strategy management. =========================================================

    public static void solve(final Problem problem) {
        assert Platform.isFxApplicationThread();
        activeProblem = problem;
        Main.getInstance().recieve(Runnable.class, problem, new HashSet<>(0), 0);
        stop();
        topScore = 0;
        go(problem);
    }

    public static void stop() {
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

    private static void go(final Problem problem) {
        if (tasks == null || tasks.isShutdown() || tasks.isTerminated()) {
            tasks = newExecutor();
        }
        // Unlike submit, execute will propagate exceptions into the FX Thread. Since
        // we use assertions to catch correctness issues, these exceptions must be seen.
        tasks.execute(new Blind(problem));
        tasks.execute(new Climb(problem));
    }

    private static ExecutorService newExecutor() {
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

    public static int getTopScore() {
        return topScore;
    }

    public static void consider(
        final Class<? extends Runnable> strategyClass,
        final Problem problem,
        final HashSet<Cell> rubbers
    ) {
        assert activeProblem != null;
        // This guard is tripped when the user uploads a new problem but worker threads
        // are still running, so workers propose solutions to the old (stale) problem.
        if (activeProblem != problem) {
            return;
        }
        final HashSet<Cell> copy = new HashSet<>(rubbers);
        assert isValidAssignment(problem, copy);
        final int score = Snake.evaluate(problem, copy);
        synchronized (Solver.class) {
            if (score > topScore) {
                topScore = score;
                Main.getInstance().recieve(strategyClass, problem, copy, score);
            }
        }
    }

    private static boolean isValidAssignment(
        final Problem problem,
        final HashSet<Cell> rubbers
    ) {
        final boolean limited = rubbers.size() <= problem.getNumRubbers();
        final boolean within = problem.getEmptyCells().containsAll(rubbers);
        return limited && within;
    }
}
