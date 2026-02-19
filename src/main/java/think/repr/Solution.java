package think.repr;

import java.util.ArrayList;
import java.util.Collection;
import think.repr.Grid.Cell;
import think.repr.Problem.Feature;
import think.tools.Iteration;

/**
    Specialization of Grid<Feature> with convenience methods for editing and protections against
    mistaken assignments. Note that there are no protections against assigning walls to exceed
    supply. In this class, "wall" refers only to Feature.PLAYER_WALL, although there are other
    types of walls.
 */
public final class Solution {

    private final Grid<Feature> backing;

    public Solution(final Solution solution) {
        this.backing = new Grid<>(solution.backing);
    }

    public Solution(final Grid<Feature> solution) {
        this.backing = new Grid<>(solution);
    }

    public void placeWalls(final Cell... cells) {
        for (final Cell cell : cells) {
            validateAndEdit(cell, Feature.EMPTY, Feature.PLAYER_WALL);
        }
    }

    public void placeWalls(final Collection<Cell> cells) {
        for (final Cell cell : cells) {
            validateAndEdit(cell, Feature.EMPTY, Feature.PLAYER_WALL);
        }
    }

    public void removeWalls(final Cell... cells) {
        for (final Cell cell : cells) {
            validateAndEdit(cell, Feature.PLAYER_WALL, Feature.EMPTY);
        }
    }

    public void removeWalls(final Collection<Cell> cells) {
        cells.forEach(cell -> validateAndEdit(cell, Feature.PLAYER_WALL, Feature.EMPTY));
    }

    public ArrayList<Cell> findWhereEmpty() {
        return Iteration.materialize(backing.where(Feature.EMPTY::equals));
    }

    public ArrayList<Cell> findWhereWall() {
        return Iteration.materialize(backing.where(Feature.PLAYER_WALL::equals));
    }

    public ArrayList<Cell> getNeighbors(final Cell cell) {
        return backing.getNeighbors(cell);
    }

    public Feature get(final Cell cell) {
        return backing.get(cell);
    }

    public int getNumRows() {
        return backing.getNumRows();
    }

    public int getNumCols() {
        return backing.getNumCols();
    }

    public int getNumWalls() {
        // PERF: Store this as a field and update it when walls are added and removed.
        return findWhereWall().size();
    }

    public Grid<Feature> getCopyOfBacking() {
        return new Grid<>(backing);
    }

    private void validateAndEdit(final Cell cell, final Feature expected, final Feature target) {
        if (backing.get(cell) != expected) {
            throw new IllegalArgumentException();
        }
        backing.set(cell, target);
    }
}
