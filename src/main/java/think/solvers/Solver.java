package think.solvers;

import infra.logging.Logger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.manager.Proposal;

/**
    Find maze configurations to solve Pathery puzzles. This abstract class provides useful getters
    and a framework to integrate its concrete subclasses with the rest of the system.
 */
public abstract class Solver implements Runnable {

    private static final AtomicInteger ID = new AtomicInteger(0);
    private final String name;
    private final Consumer<Proposal> listener;
    private final Puzzle puzzle;
    private volatile boolean alive;

    public Solver(final Consumer<Proposal> listener, final Puzzle puzzle) {
        this.name = getClass().getSimpleName() + "~" + ID.getAndIncrement();
        this.listener = listener;
        this.puzzle = puzzle;
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
        final long startNs = System.nanoTime();
        try {
            solve();
            Logger.info(
                "Terminated %s (returned normally, %d ms)",
                getClass().getSimpleName(),
                (System.nanoTime() - startNs) / 1_000_000
            );
        } catch (KilledException _) {
            Logger.info(
                "Terminated %s (killed, %d ms)",
                getClass().getSimpleName(),
                (System.nanoTime() - startNs) / 1_000_000
            );
        }
    }

    public final void requestTermination() {
        this.alive = false;
    }

    // == Subclass contract. ==

    /**
        Concrete subclasses must do all non-trivial work in this method, as opposed to the
        constructor. Implementations must call {@link #checkAlive} at least once every 500
        milliseconds or so when presented a map smaller than 20 by 20 with 10 waypoints.
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

    protected final void propose(final Tile[] state) {
        listener.accept(new Proposal(name, puzzle, state));
    }
}
