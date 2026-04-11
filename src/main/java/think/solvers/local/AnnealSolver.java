package think.solvers.local;

import java.util.function.Consumer;
import think.domain.model.Puzzle;
import think.manager.Proposal;
import think.solvers.Solver;

public final class AnnealSolver extends Solver {

    public AnnealSolver(final Consumer<Proposal> listener, final Puzzle puzzle) {
        super(listener, puzzle);
    }

    @Override
    protected void solve() throws KilledException {
        propose(getPuzzle().getTiles());
    }
}
