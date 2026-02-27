package think2.domain.repr;

import java.util.HashSet;
import java.util.Set;
import think2.graph.Graph;
import think2.graph.impl.GridGraph;
import think2.graph.impl.GridGraph.Cell;

/**
    Represents a Pathery board. Exposes limited mutation and read-only accessor methods of a backing
    GridGraph<Feature>. Boards are solutions to {@link Puzzle}s.
 */
public final class Board {

    public enum Feature {
        EMPTY,
        CHECKPOINT,
    }

    private final GridGraph<Feature> backing;
    private final Set<Cell> originallyEmpty;
    private final Set<Cell> currentWalls;

    Board(
        final GridGraph<Feature> backing,
        final Set<Cell> originallyEmpty,
        final Set<Cell> currentWalls
    ) {
        this.backing = backing.shallowCopy();
        this.originallyEmpty = originallyEmpty;
        this.currentWalls = new HashSet<>(currentWalls);
    }

    public Graph<Cell, Feature, Integer> getBacking() {
        // You can mutate the returned graph by casting to a MutableVertexGraph, but don't you dare.
        return backing;
    }

    public Set<Cell> getOriginallyEmpty() {
        return new HashSet<>(originallyEmpty);
    }

    public Set<Cell> getCurrentWalls() {
        return new HashSet<>(currentWalls);
    }

    public int getNumPlacedWalls() {
        return currentWalls.size();
    }

    public boolean placeWall(final Cell cell) {
        if (!originallyEmpty.contains(cell) || currentWalls.contains(cell)) {
            throw new IllegalArgumentException();
        }
        backing.removeVertex(cell);
        currentWalls.add(cell);
        return true;
    }

    public boolean removeWall(final Cell cell) {
        if (!originallyEmpty.contains(cell) || !currentWalls.contains(cell)) {
            throw new IllegalArgumentException();
        }
        backing.putVertex(cell, Feature.EMPTY);
        currentWalls.remove(cell);
        return true;
    }

    public Board shallowCopy() {
        return new Board(backing.shallowCopy(), originallyEmpty, new HashSet<>(currentWalls));
    }
}
