package solvers.bruteforce;

import domain.model.Board;
import domain.model.Puzzle;
import java.util.ArrayList;
import java.util.Collections;
import solvers.Solver;
import solvers.graph.impl.GridGraph.Cell;

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
