package think.solvers;

import infra.logging.Logger;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.manager.Proposal;

/**
    Find maze configurations to solve Pathery puzzles. This abstract class provides getters and a
    framework to integrate its concrete subclasses with the rest of the system.
 */
public abstract class Solver implements Runnable {

    private static final AtomicInteger ID = new AtomicInteger(0);
    private final String name;
    private final Consumer<Proposal> listener;
    private final Puzzle puzzle;
    private final CountDownLatch latch;
    private volatile Thread runner;
    private volatile boolean alive;

    public Solver(final Consumer<Proposal> listener, final Puzzle puzzle) {
        this.name = getClass().getSimpleName() + "~" + ID.getAndIncrement();
        this.listener = listener;
        this.puzzle = puzzle;
        this.latch = new CountDownLatch(1);
        this.runner = null;
        this.alive = true;
    }

    // == Public API. ==

    @Override
    public final void run() {
        // When the user instructs us to work on a different problem, we should work on it. But
        // many solvers run forever, and there is no safe way to forcefully kill a thread or
        // procedure. So, the solver needs to check when to stop. We can do so with a lengthy chain
        // of "if not alive, return", but throwing exceptions is an easier way to do non-local
        // returns.
        Logger.info("Started %s", getClass().getSimpleName());
        final long startNanos = System.nanoTime();
        runner = Thread.currentThread();
        try {
            solve();
            Logger.info(
                "Terminated %s (returned normally, %d ms)",
                getClass().getSimpleName(),
                Duration.ofNanos(System.nanoTime() - startNanos).toMillis()
            );
        } catch (KilledException _) {
            Logger.info(
                "Terminated %s (killed, %d ms)",
                getClass().getSimpleName(),
                Duration.ofNanos(System.nanoTime() - startNanos).toMillis()
            );
        } finally {
            latch.countDown();
        }
    }

    public final void requestTermination() {
        this.alive = false;
    }

    public final void awaitTermination() {
        requestTermination();
        if (runner == Thread.currentThread()) {
            throw new IllegalCallerException();
        }
        try {
            latch.await();
        } catch (InterruptedException exception) {
            throw new AssertionError(exception);
        }
    }

    // == Subclass contract. ==

    /**
        Concrete subclasses must do all non-trivial work in this method instead of the constructor.
        Implementations must call {@link #checkAlive} very often.
     */
    protected abstract void solve() throws KilledException;

    // == Protected API. ==

    protected static final class KilledException extends Exception {}

    protected final void checkAlive() throws KilledException {
        if (!alive) {
            throw new KilledException();
        }
    }

    protected final Puzzle getPuzzle() {
        return puzzle;
    }

    protected final void propose(final Tile[] state) throws KilledException {
        checkAlive();
        listener.accept(new Proposal(name, puzzle, state));
    }
}
