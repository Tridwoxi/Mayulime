package think2.solve.impl;

import java.util.ArrayList;
import think.tools.Random;
import think.tools.Random.RestrictedBinomialDistribution;
import think2.domain.repr.Board;
import think2.domain.repr.Puzzle;
import think2.graph.impl.GridGraph.Cell;
import think2.solve.Solver;

/**
    Guess randomly.

    Proof of concept. Not intended for use, except perhaps as a benchmark.
 */
public final class RandomSolver extends Solver {

    private final ArrayList<Cell> emptyCells;
    private final RestrictedBinomialDistribution numWalls;

    public RandomSolver(final ProposedSolution listener, final Puzzle puzzle) {
        super(listener, puzzle);
        this.emptyCells = new ArrayList<>(getPuzzle().getOriginal().getOriginallyEmpty());
        this.numWalls = new RestrictedBinomialDistribution(
            emptyCells.size(),
            getPuzzle().getWallBudget()
        );
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
        Random.uniformStream(emptyCells).limit(numWalls.sample()).forEachOrdered(board::placeWall);
        return board;
    }
}
