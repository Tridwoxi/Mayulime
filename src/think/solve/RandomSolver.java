package think.solve;

import think.ana.Manipulate;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;
import think.tools.Random.RestrictedBinomialDistribution;

/**
    Guess randomly.

    Proof of concept. Not intended for use, except perhaps as a benchmark.
 */
public final class RandomSolver extends Solver {

    private final RestrictedBinomialDistribution numWalls;

    public RandomSolver(final ProposedSolutionListener listener, final Problem problem) {
        super(listener, problem);
        this.numWalls = new RestrictedBinomialDistribution(
            (int) getProblem().getCachedInitial().where(Feature.EMPTY::equals).count(),
            getProblem().getPlayerWallSupply()
        );
    }

    @Override
    protected void solve() throws KilledException {
        while (true) {
            checkAlive();
            final Grid<Feature> solution = getProblem().getAnotherInitial();
            Manipulate.splatter(solution, numWalls.sample());
            proposeSolution(solution);
        }
    }
}
