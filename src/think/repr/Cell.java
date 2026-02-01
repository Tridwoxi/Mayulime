package think.repr;

import java.util.ArrayList;

/**
    A lightweight (row, col) index pair used conceptually as item = grid[row][col]. For
    JavaFX, row is Y, and col is X. Cells are not defined outside of a grid.
 */
public record Cell(int row, int col) {
    public static final Cell OUT_OF_BOUNDS = new Cell(-1, -1);

    /**
        Get neighbors of this cell on the given grid in up, right, down, left order.
     */
    public ArrayList<Cell> getNeighborsURDL(final Grid<?> grid) {
        // Potential optimization: get Cell instances from problem to reuse them,
        // reducing GC pressure. Reasonable because this method is important to BFS.
        assert grid.inBounds(this);
        final ArrayList<Cell> neighbors = new ArrayList<>(4);
        if (row - 1 >= 0) {
            neighbors.add(new Cell(row - 1, col)); // Up.
        }
        if (col + 1 < grid.getNumCols()) {
            neighbors.add(new Cell(row, col + 1)); // Right.
        }
        if (row + 1 < grid.getNumRows()) {
            neighbors.add(new Cell(row + 1, col)); // Down.
        }
        if (col - 1 >= 0) {
            neighbors.add(new Cell(row, col - 1)); // Left.
        }
        assert neighbors.stream().allMatch(this::isNeighbor);
        return neighbors;
    }

    /**
        Get the neighbors of this cell on the given grid. The order of neighbors in the
        returned list is unspecified, regardless of what the implementation indicates.
     */
    public ArrayList<Cell> getNeighbors(final Grid<?> grid) {
        return getNeighborsURDL(grid);
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
