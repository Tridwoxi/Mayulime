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

/**
    Strategy manager and bridge from backend to GUI. Must be called from JavaFX
    Application Thread. Workers run in background to not block the GUI.
 */
public final class Solver {

    private static ExecutorService tasks = newExecutor();
    private static Problem activeProblem;
    private static int bestScore = 0;

    private Solver() {}

    // == Strategy management. =========================================================

    public static void solve(final Problem problem) {
        assert Platform.isFxApplicationThread();
        activeProblem = problem;
        bestScore = 0;
        Main.recieve(problem, new HashSet<>(0), 0);
        stop();
        go(problem);
    }

    public static void stop() {
        tasks.shutdownNow();
        try {
            tasks.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private static void go(final Problem problem) {
        if (tasks.isShutdown() || tasks.isTerminated()) {
            tasks = newExecutor();
        }
        // Unlike submit, execute will propagate exceptions into the FX Thread. Since
        // we use assertions to catch correctness issues, these exceptions must be seen.
        tasks.execute(new Blind(problem));
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

    public static boolean beatsBest(final int score) {
        assert activeProblem != null;
        synchronized (Solver.class) {
            return score > bestScore;
        }
    }

    public static void claimImprovement(
        final Problem problem,
        final HashSet<Cell> rubbers,
        final int claimedScore
    ) {
        assert activeProblem != null && activeProblem == problem;
        assert isValidAssignment(problem, rubbers);
        final int actualScore = Snake.evaluate(problem, rubbers);
        assert actualScore == claimedScore;
        // Strategies should only claim to have improved upon the best solution if they
        // have actually done so. However, since this is a concurrent program and
        // evaluation is seperate from this method, strategies might lie. Such is
        // unavoidable, and we'll simply check for lies and ignore them.
        synchronized (Solver.class) {
            if (beatsBest(actualScore)) {
                bestScore = actualScore;
                Main.recieve(problem, rubbers, actualScore);
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
