package think.stra;

import java.util.function.Supplier;
import think.repr.Problem;

/**
    Before any other strategies as the Manger to consider their solutions, we send a
    blank solution, which serves as a benchmark and updates the GUI.
 */
public final class BlankSolution extends Strategy {

    public BlankSolution(
        final Considerer considerer,
        final Supplier<Integer> scorer,
        final Problem problem
    ) {
        super(considerer, scorer, problem, "doing nothing");
    }

    @Override
    protected void solve() throws KilledException {
        consider(getProblem().getCachedInitial());
    }
}
