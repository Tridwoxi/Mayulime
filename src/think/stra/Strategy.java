package think.stra;

import infra.io.Logging;
import think.ana.Snake;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;

/**
    Think of solutions to Pathery problems.

    This abstract class provides a few useful getters and a framework to integrate its
    concrete subclasses with the rest of the system.
 */
public abstract class Strategy implements Runnable {

    @FunctionalInterface
    public interface ProposedSolutionListener {
        void listen(
            String submitter,
            Problem problem,
            Grid<Feature> solution,
            int score
        );
    }

    private final ProposedSolutionListener listener;
    private final Problem problem;
    private volatile boolean alive;

    public Strategy(final ProposedSolutionListener listener, final Problem problem) {
        this.listener = listener;
        this.problem = problem;
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
        Logging.log(getClass(), "Started");
        try {
            solve();
            Logging.log(getClass(), "Terminated (returned normally)");
        } catch (final KilledException exception) {
            Logging.log(getClass(), "Terminated (killed)");
        }
    }

    public final void pleaseDie() {
        this.alive = false;
    }

    // == Subclass contract. ===========================================================

    /**
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

    protected final void proposeSolution(final Grid<Feature> solution) {
        listener.listen(
            getClass().getSimpleName(),
            problem,
            solution,
            Snake.evaluate(problem, solution)
        );
    }
}
