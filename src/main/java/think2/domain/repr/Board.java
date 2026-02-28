package think2.domain.repr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final Set<Cell> spentWalls;

    Board(final GridGraph<Feature> original) {
        this.backing = original.shallowCopy();
        this.originallyEmpty = original
            .getAllVertexKeys()
            .stream()
            .filter(cell -> original.getVertexValue(cell).equals(Feature.EMPTY))
            .collect(Collectors.toCollection(HashSet::new));
        this.spentWalls = new HashSet<>();
    }

    private Board(
        final GridGraph<Feature> backing,
        final Set<Cell> originallyEmpty,
        final Set<Cell> spentWalls
    ) {
        this.backing = backing.shallowCopy();
        this.originallyEmpty = originallyEmpty;
        this.spentWalls = new HashSet<>(spentWalls);
    }

    public Graph<Cell, Feature, Integer> getBacking() {
        // You can mutate the returned graph by casting to a MutableVertexGraph, but don't you dare.
        return backing;
    }

    public Set<Cell> getOriginallyEmpty() {
        return new HashSet<>(originallyEmpty);
    }

    public Set<Cell> getSpentWalls() {
        return new HashSet<>(spentWalls);
    }

    public List<Cell> getAllPossibleCells() {
        return new ArrayList<>(backing.getAllPossibleCells());
    }

    public int getNumSpentWalls() {
        return spentWalls.size();
    }

    public boolean placeWall(final Cell cell) {
        if (!originallyEmpty.contains(cell) || spentWalls.contains(cell)) {
            throw new IllegalArgumentException();
        }
        backing.removeVertex(cell);
        spentWalls.add(cell);
        return true;
    }

    public boolean removeWall(final Cell cell) {
        if (!originallyEmpty.contains(cell) || !spentWalls.contains(cell)) {
            throw new IllegalArgumentException();
        }
        backing.putVertex(cell, Feature.EMPTY);
        spentWalls.remove(cell);
        return true;
    }

    public Board shallowCopy() {
        return new Board(backing.shallowCopy(), originallyEmpty, new HashSet<>(spentWalls));
    }
}
