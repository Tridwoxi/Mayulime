package think;

import app.Main;
import java.util.HashSet;
import javafx.application.Platform;
import think.repr.Problem;

/**
    Problem solver and worker. Should be called from JavaFX Application Thread. Starts
    a background worker to not block the GUI.
 */
public final class Solver {

    private static Worker WORKER_INSTANCE = null;

    private Solver() {}

    public static synchronized void solve(final Problem board) {
        assert Platform.isFxApplicationThread() : "Application thread is manager.";
        if (WORKER_INSTANCE != null) {
            WORKER_INSTANCE.interrupt();
            try {
                WORKER_INSTANCE.join();
            } catch (InterruptedException e) {}
        }
        WORKER_INSTANCE = new Worker(board);
        WORKER_INSTANCE.start();
    }
}

class Worker extends Thread {

    private final Problem board;

    public Worker(final Problem board) {
        this.board = board;
    }

    @Override
    public void run() {
        Main.fromSolver(board, new HashSet<>(), 0);
    }
}
