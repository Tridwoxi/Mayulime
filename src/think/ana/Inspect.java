package think.ana;

import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;

/**
    Examine the properties of a problem, solution, or feature.
 */
public final class Inspect {

    private Inspect() {}

    public static int getNumPlacedPlayerWalls(final Grid<Feature> solution) {
        return (int) solution.where(Feature.PLAYER_WALL::equals).count();
    }

    /**
        Get a number at least as large as the maximum score that can be achieved on the
        given problem.
     */
    public static int getLoosePathLengthUpperBound(final Problem problem) {
        final int numCheckpoints = problem.getCheckpoints().size();
        final int numTeleports = problem.getTeleports().size();
        final int numOpenCells = (int) problem
            .getCachedInitial()
            .where(Inspect::isOpen)
            .count();
        // To get to a checkpoint, a snake needs to step on no more than every open
        // cell. A teleport can place the snake away by a distance no more than every
        // open cell. Hence, a loose upper bound for maximum possible length:
        return (numCheckpoints + numTeleports) * numOpenCells;
    }

    /**
        A cell is open iff it does not contain a system wall or player wall. A cell is
        empty iff it is open and not a checkpoint or teleport. Snakes may only step on
        open cells. Player walls may only be placed on empty cells. The set of open
        cells is an improper superset of the set of empty cells.
     */
    public static boolean isOpen(final Feature feature) {
        return switch (feature) {
            case EMPTY -> true;
            case CHECKPOINT -> true;
            case SYSTEM_WALL -> false;
            case PLAYER_WALL -> false;
            case TELEPORT_IN -> true;
            case TELEPORT_OUT -> true;
        };
    }
}
