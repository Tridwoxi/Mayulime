package think.graph.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.function.Function;
import think.graph.Graph.MutableVertexGraph;
import think.graph.impl.GridGraph.Cell;

/**
    ArrayList-backed implementation of a two-dimensional grid graph with holes. This is a highly
    specialized graph and should not be used for typical purposes. A vertex can be added iff it is
    in bounds. Grid graphs are indexed by a {@link Cell}, which is a {@code (row, col)}, the same
    way an array of arrays is indexed in Java. Grid graphs have Integer edges of value 1.

    Children and parents are both returned in up, right, down, left order (see Pathery snake
    preference order). Vertex keys are returned in lexicographic order (how you would typically
    traverse an array of arrays).
 */
public final class GridGraph<V> implements MutableVertexGraph<Cell, V, Integer> {

    public record Cell(int row, int col) implements Comparable<Cell> {
        @Override
        public int compareTo(final Cell other) {
            final int byRow = Integer.compare(row, other.row);
            return byRow != 0 ? byRow : Integer.compare(col, other.col);
        }

        private boolean isNeighbor(final Cell other) {
            return Math.abs(row - other.row) + Math.abs(col - other.col) == 1;
        }
    }

    private static final Integer EDGE_LENGTH = 1;
    private final List<Cell> allCells;
    private final List<Optional<V>> backing; // Index as `point.row * numCols + point.col`.
    private final int numCols;
    private final int numRows;

    public GridGraph(
        final int numRows,
        final int numCols,
        final Function<Cell, Optional<V>> factory
    ) {
        this.allCells = new ArrayList<>(numRows * numCols);
        this.backing = new ArrayList<>(numRows * numCols);
        this.numCols = numCols;
        this.numRows = numRows;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                allCells.add(new Cell(row, col));
                backing.add(factory.apply(allCells.getLast()));
            }
        }
    }

    // == Graph interface. =========================================================================

    @Override
    public boolean containsVertexKey(final Cell vertexKey) {
        return isInBounds(vertexKey) && isPresent(vertexKey);
    }

    @Override
    public V getVertexValue(final Cell vertexKey) {
        throwIfNotContains(vertexKey);
        return backing.get(toIndex(vertexKey)).orElseThrow();
    }

    @Override
    public boolean containsEdge(final Cell sourceKey, final Cell destinationKey) {
        throwIfNotContains(sourceKey);
        throwIfNotContains(destinationKey);
        return sourceKey.isNeighbor(destinationKey);
    }

    @Override
    public Integer getEdge(final Cell sourceKey, final Cell destinationKey) {
        if (!containsEdge(sourceKey, destinationKey)) {
            throw new NoSuchElementException();
        }
        return EDGE_LENGTH;
    }

    @Override
    public SequencedSet<Cell> getChildren(final Cell parentKey) {
        throwIfNotContains(parentKey);
        // PERF: Dear gods of streaming, object allocation, and linked hash sets, can we write an
        // implementation that's any worse? I fear that what we have done here will enable one run
        // of breadth-first search every few decades or so, and that's much too fast for us, eh?
        // (A VisualVM profile indicated a previous implementation of this method accounted for
        // ~20% of runtime on Simples under random guessing; this implementation is much slower.)
        final List<Cell> candidates = List.of(
            new Cell(parentKey.row() - 1, parentKey.col()), // Up.
            new Cell(parentKey.row(), parentKey.col() + 1), // Right.
            new Cell(parentKey.row() + 1, parentKey.col()), // Down.
            new Cell(parentKey.row(), parentKey.col() - 1) // Left.
        );
        return toSequencedSetIfContained(candidates);
    }

    @Override
    public SequencedSet<Cell> getParents(final Cell childKey) {
        return getChildren(childKey);
    }

    @Override
    public SequencedSet<Cell> getAllVertexKeys() {
        return toSequencedSetIfContained(allCells);
    }

    @Override
    public GridGraph<V> shallowCopy() {
        return new GridGraph<>(numRows, numCols, vertexKey ->
            containsVertexKey(vertexKey) ? Optional.of(getVertexValue(vertexKey)) : Optional.empty()
        );
    }

    @Override
    public boolean putVertex(final Cell vertexKey, final V vertexValue) {
        if (!isInBounds(vertexKey)) {
            throw new IllegalArgumentException();
        }
        final Optional<V> previous = backing.get(toIndex(vertexKey));
        if (!previous.equals(Optional.of(vertexValue))) {
            backing.set(toIndex(vertexKey), Optional.of(vertexValue));
            return true;
        }
        return false;
    }

    @Override
    public boolean removeVertex(final Cell vertexKey) {
        if (!containsVertexKey(vertexKey)) {
            return false;
        }
        backing.set(toIndex(vertexKey), Optional.empty());
        return true;
    }

    private SequencedSet<Cell> toSequencedSetIfContained(final List<Cell> candidates) {
        final SequencedSet<Cell> result = new LinkedHashSet<>(candidates.size(), 1.0f);
        candidates.stream().filter(this::containsVertexKey).forEachOrdered(result::add);
        return result;
    }

    private void throwIfNotContains(final Cell vertexKey) {
        if (!containsVertexKey(vertexKey)) {
            throw new NoSuchElementException();
        }
    }

    // == Grid interface. ==========================================================================

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public SequencedSet<Cell> getAllPossibleCells() {
        return new LinkedHashSet<>(allCells);
    }

    public boolean isInBounds(final Cell cell) {
        return cell.row() >= 0 && cell.row() < numRows && cell.col() >= 0 && cell.col() < numCols;
    }

    private boolean isPresent(final Cell vertexKey) {
        // Any caller of this method assumes the Cell is in bounds. Internal use only.
        return backing.get(toIndex(vertexKey)).isPresent();
    }

    private int toIndex(final Cell cell) {
        return cell.row() * numCols + cell.col();
    }
}
