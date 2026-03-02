package think.domain.repr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import think.domain.repr.Board.Feature;
import think.graph.Graph;
import think.graph.impl.GridGraph;
import think.graph.impl.GridGraph.Cell;

/**
    Pathery problem specification. Contains metadata and empty board supplier. The {@code
    getOriginally*} methods return cells in grid traversal order.
 */
public final class Puzzle {

    private final String name;
    private final Board original;
    private final List<Cell> checkpoints;
    private final SortedSet<Cell> originallyEmpty;
    private final SortedSet<Cell> originallyMissing;
    private final SortedSet<Cell> originallyCheckpoint;
    private final int wallBudget;

    public Puzzle(
        final String name,
        final GridGraph<Feature> original,
        final List<Cell> checkpoints,
        final int wallBudget
    ) {
        final Function<Predicate<Optional<Feature>>, SortedSet<Cell>> findWhere = predicate -> {
            final Function<Cell, Optional<Feature>> optionalGet = cell ->
                original.containsVertexKey(cell)
                    ? Optional.of(original.getVertexValue(cell))
                    : Optional.empty();
            return original
                .getAllPossibleCells()
                .stream()
                .filter(cell -> predicate.test(optionalGet.apply(cell)))
                .collect(Collectors.toCollection(TreeSet::new));
        };
        this.name = name;
        this.original = new Board(original);
        this.checkpoints = new ArrayList<>(checkpoints);
        this.wallBudget = wallBudget;
        this.originallyEmpty = findWhere.apply(Optional.of(Feature.EMPTY)::equals);
        this.originallyMissing = findWhere.apply(Optional.empty()::equals);
        this.originallyCheckpoint = findWhere.apply(Optional.of(Feature.CHECKPOINT)::equals);
    }

    public String getName() {
        return name;
    }

    public Board getBoard() {
        return original.shallowCopy();
    }

    public List<Cell> getCheckpoints() {
        return new ArrayList<>(checkpoints);
    }

    public int getWallBudget() {
        return wallBudget;
    }

    public SortedSet<Cell> getOriginallyEmpty() {
        return new TreeSet<>(originallyEmpty);
    }

    public SortedSet<Cell> getOriginallyMissing() {
        return new TreeSet<>(originallyMissing);
    }

    public SortedSet<Cell> getOriginallyCheckpoint() {
        return new TreeSet<>(originallyCheckpoint);
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
