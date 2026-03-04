package solvers.baseline;

import domain.old_model.Puzzle;
import solvers.Solver;

/**
    Hack used by {@link solvers.Manager} to send baseline results.
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
