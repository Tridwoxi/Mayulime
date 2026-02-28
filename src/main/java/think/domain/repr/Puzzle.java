package think.domain.repr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import think.domain.repr.Board.Feature;
import think.graph.Graph;
import think.graph.impl.GridGraph;
import think.graph.impl.GridGraph.Cell;

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

    public boolean isValid(final Board board) {
        // You could probably maliciously defeat this check, but I think doing so requires a bit of
        // inventiveness. So far, it's just been an accident detector, and I think it'll suffice.
        final Graph<Cell, Feature, Integer> originalBacking = original.getBacking();
        final Graph<Cell, Feature, Integer> candidateBacking = board.getBacking();
        final Predicate<Cell> okay = cell -> {
            final Feature originalFeature = originalBacking.getVertexValue(cell);
            return switch (originalFeature) {
                case EMPTY -> !candidateBacking.containsVertexKey(cell) ||
                candidateBacking.getVertexValue(cell) == Feature.EMPTY;
                case CHECKPOINT -> candidateBacking.containsVertexKey(cell) &&
                candidateBacking.getVertexValue(cell) == Feature.CHECKPOINT;
            };
        };
        final boolean matching = originalBacking.getAllVertexKeys().stream().allMatch(okay);
        final boolean permissible = board.getNumSpentWalls() <= wallBudget;
        return matching && permissible;
    }
}
