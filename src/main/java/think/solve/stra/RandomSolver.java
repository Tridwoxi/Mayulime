package think.solve.stra;

import java.util.ArrayList;
import think.domain.repr.Board;
import think.domain.repr.Puzzle;
import think.graph.impl.GridGraph.Cell;
import think.solve.Solver;
import think.solve.tools.Iteration;
import think.solve.tools.RestrictedBinomial;

/**
    Guess randomly.

    Proof of concept. Not intended for use, except perhaps as a benchmark.
 */
public final class RandomSolver extends Solver {

    private final ArrayList<Cell> emptyCells;
    private final RestrictedBinomial numWalls;

    public RandomSolver(final ProposedSolution listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.emptyCells = new ArrayList<>(getPuzzle().getOriginal().getOriginallyEmpty());
        this.numWalls = new RestrictedBinomial(emptyCells.size(), getPuzzle().getWallBudget());
    }

    @Override
    protected void solve() throws KilledException {
        while (true) {
            checkAlive();
            getListener().listen(getClass().getSimpleName(), getPuzzle(), createRandomBoard());
        }
    }

    private Board createRandomBoard() {
        final Board board = getPuzzle().getOriginal();
        Iteration.randomly(emptyCells).limit(numWalls.sample()).forEach(board::placeWall);
        return board;
    }
}
