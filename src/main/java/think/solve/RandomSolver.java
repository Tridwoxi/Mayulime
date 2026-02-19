package think.solve;

import java.util.ArrayList;
import think.repr.Grid.Cell;
import think.repr.Problem;
import think.repr.Solution;
import think.tools.Random;
import think.tools.Random.RestrictedBinomialDistribution;

/**
    Guess randomly.

    Proof of concept. Not intended for use, except perhaps as a benchmark.
 */
public final class RandomSolver extends Solver {

    private final ArrayList<Cell> emptyCells;
    private final RestrictedBinomialDistribution numWalls;

    public RandomSolver(final ProposedSolutionListener listener, final Problem problem) {
        super(listener, problem);
        this.emptyCells = getProblem().getBlankSolution().findWhereEmpty();
        this.numWalls = new RestrictedBinomialDistribution(
            emptyCells.size(),
            getProblem().getPlayerWallSupply()
        );
    }

    @Override
    protected void solve() throws KilledException {
        while (true) {
            checkAlive();
            proposeSolution(createRandomSolution());
        }
    }

    private Solution createRandomSolution() {
        final Solution solution = getProblem().getBlankSolution();
        Random.uniformStream(emptyCells)
            .limit(numWalls.sample())
            .forEachOrdered(solution::placeWalls);
        return solution;
    }
}
