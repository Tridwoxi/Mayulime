package think.solve;

import java.util.ArrayList;
import think.repr.Cell;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;
import think.tools.Iteration;
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
        this.emptyCells = Iteration.materialize(
            getProblem().getCachedInitial().where(Feature.EMPTY::equals)
        );
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

    private Grid<Feature> createRandomSolution() {
        final Grid<Feature> candidateSolution = getProblem().getAnotherInitial();
        Random.uniformStream(emptyCells)
            .limit(numWalls.sample())
            .forEachOrdered(cell -> candidateSolution.set(cell, Feature.PLAYER_WALL));
        return candidateSolution;
    }
}
