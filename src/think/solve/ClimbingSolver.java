package think.solve;

import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;

/**
    Random restart hill climbing.
 */
public final class ClimbingSolver extends Solver {

    public ClimbingSolver(
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
