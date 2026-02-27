package think2.solve.impl;

import think2.domain.repr.Puzzle;
import think2.solve.Solver;

/**
    Hack used by {@link think2.Manager} to send baseline results.
 */
public final class BaselineSolver extends Solver {

    public BaselineSolver(final ProposedSolution listener, final Puzzle puzzle) {
        super(listener, puzzle);
    }

    @Override
    protected void solve() throws KilledException {
        throw new UnsupportedOperationException();
    }
}
