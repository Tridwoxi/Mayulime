package think.repr;

import java.util.ArrayList;

/**
    A lightweight (row, col) index pair used conceptually as item = grid[row][col]. For
    JavaFX, row is Y, and col is X. Cells are not defined outside of a grid.
 */
public record Cell(int row, int col) {
    public static final Cell OUT_OF_BOUNDS = new Cell(-1, -1);

    /**
        From the task specification: "Among shortest paths, the Snake prefers to go up,
        then right, then down, then left.". We return neighbors in that order.
     */
    public ArrayList<Cell> getNeighborsOn(final Grid<?> grid) {
        // Potential optimization: get Cell instances from problem to reuse them,
        // reducing GC pressure. Reasonable because this method is important to BFS.
        assert grid.inBounds(this);
        final ArrayList<Cell> neighbors = new ArrayList<>(4);
        final int numRows = grid.getNumRows();
        final int numCols = grid.getNumCols();
        if (row - 1 >= 0) {
            neighbors.add(new Cell(row - 1, col));
        }
        if (col + 1 < numCols) {
            neighbors.add(new Cell(row, col + 1));
        }
        if (row + 1 < numRows) {
            neighbors.add(new Cell(row + 1, col));
        }
        if (col - 1 >= 0) {
            neighbors.add(new Cell(row, col - 1));
        }
        assert neighbors.stream().allMatch(this::isNeighbor);
        return neighbors;
    }

    /**
        Two cells are neighbors iff they are adjacent horizontally or vertically (Von
        Neumann neighborhood). Cells are not neighbors of themselves. Neighbor-ness is
        symmetric. Assumes but does not verify that cells are on the same problem.
     */
    public boolean isNeighbor(final Cell other) {
        return manhattanTo(other) == 1;
    }

    public int manhattanTo(final Cell other) {
        return Math.abs(this.row - other.row) + Math.abs(this.col - other.col);
    }
}
