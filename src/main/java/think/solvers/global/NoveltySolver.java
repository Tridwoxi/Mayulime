package think.solvers.global;

import java.util.function.Consumer;
import think.domain.model.Puzzle;
import think.manager.Proposal;
import think.solvers.Solver;

public final class NoveltySolver extends Solver {

    public NoveltySolver(final Consumer<Proposal> listener, final Puzzle puzzle) {
        super(listener, puzzle);
    }

    @Override
    protected void solve() throws KilledException {
        propose(getPuzzle().getFeatures());
    }
}
