package think;

import javafx.application.Platform;
import think.repr.Problem;
import think.stra.Blind;

/**
    Problem solver and worker. Should be called from JavaFX Application Thread. Starts
    a background worker to not block the GUI.
 */
public final class Solver {

    private static Worker WORKER_INSTANCE = null;

    private Solver() {}

    public static synchronized void solve(final Problem problem) {
        assert Platform.isFxApplicationThread();
        if (WORKER_INSTANCE != null) {
            WORKER_INSTANCE.interrupt();
            try {
                WORKER_INSTANCE.join();
            } catch (InterruptedException e) {}
        }
        WORKER_INSTANCE = new Worker(problem);
        WORKER_INSTANCE.start();
    }
}

final class Worker extends Thread {

    private final Problem problem;

    public Worker(final Problem problem) {
        this.problem = problem;
    }

    @Override
    public void run() {
        new Blind(problem);
    }
}
