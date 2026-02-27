package think2.domain.repr;

import java.util.ArrayList;
import java.util.List;
import think2.domain.repr.Board.Feature;
import think2.graph.impl.GridGraph;
import think2.graph.impl.GridGraph.Cell;

/**
    Pathery problem specification. Contains metadata and empty board supplier.
 */
public final class Puzzle {

    private final String name;
    private final Board original;
    private final List<Cell> checkpoints;
    private final int wallBudget;

    public Puzzle(
        final String name,
        final GridGraph<Feature> original,
        final List<Cell> checkpoints,
        final int wallBudget
    ) {
        this.name = name;
        this.original = new Board(original);
        this.checkpoints = List.copyOf(checkpoints);
        this.wallBudget = wallBudget;
    }

    public String getName() {
        return name;
    }

    public Board getOriginal() {
        return original.shallowCopy();
    }

    public List<Cell> getCheckpoints() {
        return new ArrayList<>(checkpoints);
    }

    public int getWallBudget() {
        return wallBudget;
    }
}
