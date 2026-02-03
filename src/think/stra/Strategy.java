package think.stra;

import think.repr.Problem;
import think.tools.Logging;

/**
    Base class for working on a problem.
 */
public abstract class Strategy implements Runnable {

    private final Problem problem;
    private final String name;
    private volatile boolean alive;

    public Strategy(final Problem problem, final String name) {
        this.problem = problem;
        this.name = name;
        this.alive = true;
    }

    @Override
    public final void run() {
        // When the user instructs us to work on a different problem, we should work on
        // it. But many strategies run forever, and there is no safe way to forcefully
        // kill a thread or procedure. So, the strategy needs to check when to stop. We
        // can do so with a lengthy chain of "if not alive, return", but throwing
        // exceptions is an easier way to do non-local returns.
        Logging.log(getClass(), "Started.");
        try {
            solve();
            Logging.log(getClass(), "Done (returned).");
        } catch (final KilledException exception) {
            Logging.log(getClass(), "Done (killed).");
        }
    }

    public final void pleaseDie() {
        this.alive = false;
    }

    public final String getName() {
        return name;
    }

    /**
        Generate solutions and ask the {@link think.Manager} to consider them.

        Concrete subclasses should do all non-trivial work in this method, as opposed
        to the constructor. Implementations of this method are free to hang
        indefintely, but it would be nice if this method calls {@link #checkAlive} at
        least once every 500 miliseconds.
     */
    protected abstract void solve() throws KilledException;

    protected final Problem getProblem() {
        return problem;
    }

    protected final void checkAlive() throws KilledException {
        if (!alive) {
            throw new KilledException();
        }
    }

    protected static final class KilledException extends Exception {}
}
