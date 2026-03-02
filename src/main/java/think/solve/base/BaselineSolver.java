package think.solve.base;

import think.domain.repr.Puzzle;
import think.solve.Solver;

/**
    Hack used by {@link think.Manager} to send baseline results.
 */
public final class BaselineSolver extends Solver {

    public BaselineSolver(final ProposedSolution listener, final Puzzle puzzle) {
        super(listener, puzzle);
    }

    @Override
    protected void solve() throws KilledException {
        checkAlive();
        getListener().listen(getClass().getSimpleName(), getPuzzle(), getPuzzle().getBoard());
    }
}
