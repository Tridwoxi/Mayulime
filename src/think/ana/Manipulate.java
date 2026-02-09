package think.ana;

import java.util.ArrayList;
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

    public static void splatter(final Grid<Feature> solution, final int howMany) {
        // Lazy solution.where(...).limit(int) will not work because we need to sample
        // the whole distribution. So materialization is mandatory.
        final ArrayList<Cell> locations = Iteration.materialize(
            solution.where(Feature.EMPTY::equals)
        );
        assert howMany <= locations.size();
        Random.uniformStream(locations)
            .limit(howMany)
            .forEachOrdered(cell -> solution.set(cell, Feature.PLAYER_WALL));
    }
}
