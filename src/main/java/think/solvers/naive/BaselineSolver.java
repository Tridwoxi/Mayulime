package think.solvers.naive;

import java.util.function.BiConsumer;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.solvers.Solver;

public final class BaselineSolver extends Solver {

    public BaselineSolver(final BiConsumer<String, Tile[]> listener, final Puzzle puzzle) {
        super(listener, puzzle);
    }

    @Override
    protected void solve() throws KilledException {
        propose(getPuzzle().tiles());
    }
}
