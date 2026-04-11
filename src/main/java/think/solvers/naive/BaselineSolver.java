package think.solvers.naive;

import java.util.function.Consumer;
import think.domain.model.Puzzle;
import think.manager.Proposal;
import think.solvers.Solver;

public final class BaselineSolver extends Solver {

    public BaselineSolver(final Consumer<Proposal> listener, final Puzzle puzzle) {
        super(listener, puzzle);
    }

    @Override
    protected void solve() throws KilledException {
        propose(getPuzzle().getTiles());
    }
}
