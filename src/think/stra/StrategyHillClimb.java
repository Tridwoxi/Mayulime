package think.stra;

import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;

/**
    Random restart hill climbing.
 */
public final class StrategyHillClimb extends Strategy {

    public StrategyHillClimb(
        final ProposedSolutionListener listener,
        final Problem problem
    ) {
        super(listener, problem);
    }

    @Override
    protected void solve() throws KilledException {
        while (true) {
            checkAlive();
            proposeSolution(hillClimb());
        }
    }

    private Grid<Feature> hillClimb() {
        return getProblem().getAnotherInitial();
    }
}
