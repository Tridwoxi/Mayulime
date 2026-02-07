package think.stra;

import think.ana.Manipulate;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;

/**
    Guess randomly.

    Proof of concept. Not intended for use, except perhaps as a benchmark.
 */
public final class StrategyGuessRandomly extends Strategy {

    public StrategyGuessRandomly(
        final ProposedSolutionListener proposedSolutionListener,
        final Problem problem
    ) {
        super(proposedSolutionListener, problem);
    }

    @Override
    protected void solve() throws KilledException {
        while (true) {
            checkAlive();
            final Grid<Feature> solution = getProblem().getAnotherInitial();
            Manipulate.splatter(solution, getProblem().getPlayerWallSupply());
            proposeSolution(solution);
        }
    }
}
