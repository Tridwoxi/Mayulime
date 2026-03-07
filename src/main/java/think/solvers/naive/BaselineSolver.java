package think.solvers.naive;

import think.domain.model.Puzzle;
import think.solvers.Solver;

public final class BaselineSolver extends Solver {

    public BaselineSolver(final ProposedSolution listener, final Puzzle puzzle) {
        super(listener, puzzle);
    }

    @Override
    protected void solve() throws KilledException {
        propose(getPuzzle().getFeatures());
    }
}
