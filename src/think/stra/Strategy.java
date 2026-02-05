package think.stra;

import java.util.function.Supplier;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;
import think.tools.Logging;

/**
    Base class for working on a problem.
 */
public abstract class Strategy implements Runnable {

    /**
        Solution listener.

        Consider that the solution may be an improvement over previous results.
     */
    @FunctionalInterface
    public interface Considerer {
        void consider(Strategy submitter, Problem problem, Grid<Feature> solution);
    }

    private final Supplier<Integer> scorer;
    private final Considerer considerer;
    private final Problem problem;
    private final String name;
    private volatile boolean alive;

    public Strategy(
        final Considerer considerer,
        final Supplier<Integer> scorer,
        final Problem problem,
        final String name
    ) {
        this.considerer = considerer;
        this.scorer = scorer;
        this.problem = problem;
        this.name = name;
        this.alive = true;
    }

    // == Public API. ==================================================================

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

    // == Subclass contract. ===========================================================

    /**
        Generate solutions and consider them.

        Concrete subclasses should do all non-trivial work in this method, as opposed
        to the constructor. Implementations of this method are free to hang
        indefintely, but it would be nice if this method calls {@link #checkAlive} at
        least once every 500 miliseconds.
     */
    protected abstract void solve() throws KilledException;

    // == Protected API. ===============================================================

    protected static final class KilledException extends Exception {}

    protected final void checkAlive() throws KilledException {
        if (!alive) {
            throw new KilledException();
        }
    }

    protected final Problem getProblem() {
        return problem;
    }

    protected final int getTopScore() {
        return scorer.get();
    }

    protected final void consider(final Grid<Feature> solution) {
        considerer.consider(this, problem, solution);
    }
}
