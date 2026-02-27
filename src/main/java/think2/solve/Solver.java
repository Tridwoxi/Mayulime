package think2.solve;

import infra.io.Logging;
import think2.domain.repr.Board;
import think2.domain.repr.Puzzle;

/**
    Find board configurations to solve Pathery puzzles. This abstract class provides useful getters
    and a framework to integrate its concrete subclasses with the rest of the system.
 */
public abstract class Solver implements Runnable {

    @FunctionalInterface
    public interface ProposedSolution {
        void listen(String submitter, Puzzle puzzle, Board solution);
    }

    private final ProposedSolution listener;
    private final Puzzle puzzle;
    private volatile boolean alive;

    public Solver(final ProposedSolution listener, final Puzzle puzzle) {
        this.listener = listener;
        this.puzzle = puzzle;
        this.alive = true;
    }

    // == Public API. =============================================================================

    @Override
    public final void run() {
        // When the user instructs us to work on a different problem, we should work on it. But
        // many solvers run forever, and there is no safe way to forcefully kill a thread or
        // procedure. So, the solver needs to check when to stop. We can do so with a lengthy chain
        // of "if not alive, return", but throwing exceptions is an easier way to do non-local
        // returns.
        Logging.info("Started %s", getClass().getSimpleName());
        try {
            solve();
            Logging.info("Terminated %s (returned normally)", getClass().getSimpleName());
        } catch (final KilledException exception) {
            Logging.info("Terminated %s (killed)", getClass().getSimpleName());
        }
    }

    public final void pleaseDie() {
        this.alive = false;
    }

    // == Subclass contract. ======================================================================

    /**
        Concrete subclasses should do all non-trivial work in this method, as opposed to the
        constructor. Implementations of this method are free to hang indefintely, but it would be
        nice if this method calls {@link #checkAlive} at least once every 500 miliseconds.
     */
    protected abstract void solve() throws KilledException;

    // == Protected API. ==========================================================================

    protected static final class KilledException extends Exception {}

    protected final void checkAlive() throws KilledException {
        if (!alive) {
            throw new KilledException();
        }
    }

    protected final Puzzle getPuzzle() {
        return puzzle;
    }

    protected final ProposedSolution getListener() {
        return listener;
    }
}
