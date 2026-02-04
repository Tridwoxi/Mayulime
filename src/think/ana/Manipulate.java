package think.ana;

import java.util.ArrayList;
import java.util.Collection;
import think.repr.Cell;
import think.repr.Grid;
import think.repr.Problem.Feature;
import think.tools.Iteration;
import think.tools.Random;

/**
    Pathery solution fiddler.

    This class consists only of static helpers that manipulate a Grid<Feature>. All
    provided methods modify the grid in-place, return void, and do not modify their
    other arguments.
 */
public final class Manipulate {

    private Manipulate() {}

    public static void emptyToPlayer(
        final Grid<Feature> solution,
        final Collection<Cell> locations
    ) {
        assert locations.stream().map(solution::get).allMatch(Feature.EMPTY::equals);
        locations.forEach(cell -> solution.set(cell, Feature.PLAYER_WALL));
    }

    public static void playerToEmpty(
        final Grid<Feature> solution,
        final Collection<Cell> locations
    ) {
        assert locations
            .stream()
            .map(solution::get)
            .allMatch(Feature.PLAYER_WALL::equals);
        locations.forEach(cell -> solution.set(cell, Feature.EMPTY));
    }

    public static void splatter(final Grid<Feature> solution, final int howManyAtMost) {
        // Lazy solution.where(...).limit(int) will not work because we need to sample
        // the whole distribution. So materialization is mandatory.
        final ArrayList<Cell> locations = Iteration.materialize(
            solution.where(Feature.EMPTY::equals)
        );
        Random.uniformStream(locations)
            .limit(howManyAtMost)
            .forEachOrdered(cell -> solution.set(cell, Feature.PLAYER_WALL));
    }
}
