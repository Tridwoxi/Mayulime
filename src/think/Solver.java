package think;

import app.Main;
import java.util.HashSet;
import javafx.application.Platform;
import think.repr.Board;

/**
    Problem solver and worker. Should be called from JavaFX Application Thread. Starts
    a background worker to not block the GUI. If multiple solvers exist, they all
    manage the same worker.
 */
public final class Solver extends Thread {

    private static volatile Solver WORKER_INSTANCE = null;
    private final Board board;

    private Solver(Board board) {
        this.board = board;
    }

    public static synchronized void solve(Board board) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException();
        }
        if (WORKER_INSTANCE != null) {
            WORKER_INSTANCE.interrupt();
            try {
                WORKER_INSTANCE.join();
            } catch (InterruptedException e) {}
        }
        WORKER_INSTANCE = new Solver(board);
        WORKER_INSTANCE.start();
    }

    @Override
    public void run() {
        Main.fromSolver(board, new HashSet<>(), 0);
    }
}
