package think.solve;

import java.util.concurrent.atomic.AtomicInteger;
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

    private Grid<Feature> hillClimb() throws KilledException {
        final Grid<Feature> candidateSolution = getProblem().getAnotherInitial();
        final AtomicInteger remainingSupply = new AtomicInteger(
            getProblem().getPlayerWallSupply()
        );
        // The methods in this loop operate by side effects and return if they were
        // successful. This loop must terminate because both placeAdditionalWalls and
        // relocateExistingWalls improve the score and there is an upper bound on
        // score. reclaimUselessWalls does not improve the score, but is idempotent.
        while (
            placeAdditionalWalls(candidateSolution, remainingSupply) ||
            relocateExistingWalls(candidateSolution, remainingSupply) ||
            reclaimUselessWalls(candidateSolution, remainingSupply)
        ) {
            checkAlive();
        }
        return candidateSolution;
    }

    private static boolean placeAdditionalWalls(
        final Grid<Feature> candidateSolution,
        final AtomicInteger remainingSupply
    ) {
        return false;
    }

    private static boolean relocateExistingWalls(
        final Grid<Feature> candidateSolution,
        final AtomicInteger remainingSupply
    ) {
        return false;
    }

    private static boolean reclaimUselessWalls(
        final Grid<Feature> candidateSolution,
        final AtomicInteger remainingSupply
    ) {
        return false;
    }
}
