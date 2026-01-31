package think.stra;

import think.Manager;
import think.repr.Problem;

/**
    Before any other strategies as the Manger to consider their solutions, we send a
    blank solution, which serves as a benchmark and updates the GUI.
 */
public final class BlankSolution extends Strategy {

    public BlankSolution(final Problem problem) {
        super(problem, "doing nothing");
    }

    @Override
    public void run() {
        Manager.getInstance().consider(
            this,
            getProblem(),
            getProblem().getCachedInitial()
        );
    }
}
