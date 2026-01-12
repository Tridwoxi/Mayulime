package think;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import think.repr.Problem;
import think.stra.Blind;

/**
    Problem solver and worker. Should be called from JavaFX Application Thread. Starts
    background workers to not block the GUI.
 */
public final class Solver {

    private static ExecutorService tasks = Executors.newCachedThreadPool();

    private Solver() {}

    public static void solve(final Problem problem) {
        assert Platform.isFxApplicationThread();
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
}
