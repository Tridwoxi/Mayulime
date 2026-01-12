package think;

import java.util.ArrayList;
import javafx.application.Platform;
import think.repr.Problem;
import think.stra.Blind;

/**
    Problem solver and worker. Should be called from JavaFX Application Thread. Starts
    a background worker to not block the GUI.
 */
public final class Solver {

    private static final ArrayList<Thread> ACTIVE_WORKERS = new ArrayList<>();

    private Solver() {}

    public static synchronized void solve(final Problem problem) {
        assert Platform.isFxApplicationThread();
        stopWorkers();
        buildWorkers(problem);
        startWorkers();
    }

    private static void stopWorkers() {
        for (final Thread worker : ACTIVE_WORKERS) {
            worker.interrupt();
            try {
                worker.join();
            } catch (InterruptedException e) {}
        }
        ACTIVE_WORKERS.clear();
    }

    private static void buildWorkers(final Problem problem) {
        ACTIVE_WORKERS.add(
            new Thread() {
                @Override
                public void run() {
                    new Blind(problem);
                }
            }
        );
    }

    private static void startWorkers() {
        for (final Thread worker : ACTIVE_WORKERS) {
            worker.start();
        }
    }
}
