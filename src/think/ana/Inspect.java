package think.ana;

import think.repr.Grid;
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
