package think;

import app.Main;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import think.ana.Snake;
import think.ana.Tools;
import think.ana.Tools.Pair;
import think.repr.Cell;
import think.repr.Problem;
import think.repr.Route;
import think.stra.Blind;

/**
    Problem solver and worker. Should be called from JavaFX Application Thread. Starts
    background workers to not block the GUI.
 */
public final class Solver {

    private static ExecutorService tasks = Executors.newCachedThreadPool();
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void go(final Problem problem) {
        if (tasks.isShutdown() || tasks.isTerminated()) {
            tasks = Executors.newCachedThreadPool();
        }
        tasks.submit(() -> new Blind(problem));
    }

    // == Communication. ===============================================================

    public static boolean beatsBest(final int score) {
        assert activeProblem != null;
        synchronized (Solver.class) {
            return score > bestScore;
        }
    }

    public static void claimSolution(
        final Problem problem,
        final HashSet<Cell> rubbers,
        final int claimedScore
    ) {
        assert activeProblem != null && activeProblem == problem;
        assert isValidAssignment(problem, rubbers);
        final int actualScore = eval(problem, rubbers);
        assert actualScore == claimedScore;
        // Strategies may occasionally falsely claim their score beats the bestScore
        // because of concurrency problems. This is fine, and we'll just ignore that.
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

    private static int eval(final Problem problem, final HashSet<Cell> rubbers) {
        int sum = 0;
        final Snake snake = new Snake();
        for (final Pair<Cell> p : Tools.pairwise(problem.getCheckpoints()).toList()) {
            final Route route = snake.travel(problem, rubbers, p.a(), p.b());
            if (!route.possible()) {
                return 0;
            }
            sum += route.length();
        }
        return sum;
    }
}
