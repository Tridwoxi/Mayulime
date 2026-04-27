package think.solvers.compass;

import java.util.function.BiConsumer;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.solvers.Solver;

public final class CompassSolver extends Solver {

    public CompassSolver(final BiConsumer<String, Tile[]> listener, final Puzzle puzzle) {
        super(listener, puzzle);
    }

    @Override
    protected void solve() throws KilledException {
        propose(getPuzzle().tiles());
    }
}
