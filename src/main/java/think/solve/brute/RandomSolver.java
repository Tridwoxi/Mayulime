package think.solve.brute;

import java.util.ArrayList;
import java.util.Collections;
import think.domain.repr.Board;
import think.domain.repr.Puzzle;
import think.graph.impl.GridGraph.Cell;
import think.solve.Solver;

/**
    Guess randomly.

    Proof of concept. Not intended for use, except perhaps as a benchmark.
 */
public final class RandomSolver extends Solver {

    private final ArrayList<Cell> emptyCells;
    private final RestrictedBinomial numWalls;

    public RandomSolver(final ProposedSolution listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.emptyCells = new ArrayList<>(getPuzzle().getOriginallyEmpty());
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
        final Board board = getPuzzle().getBoard();
        final int toPlace = Math.min(emptyCells.size(), numWalls.sample());
        Collections.shuffle(emptyCells);
        for (int index = 0; index < toPlace; index += 1) {
            board.placeWall(emptyCells.get(index));
        }
        return board;
    }
}
