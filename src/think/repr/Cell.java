package think.repr;

import java.util.ArrayList;

/**
    A lightweight (row, col) index pair used conceptually as grid.get(row).get(col). For
    JavaFX, row is Y, and col is X. Cells are not defined outside of a grid.
 */
public record Cell(int row, int col) {
    public ArrayList<Cell> getNeighbors(final Problem problem) {
        // Potential optimization: get Cell instances from problem to reuse them,
        // reducing GC pressure. Reasonable because this method is important to BFS.
        assert problem.containsCell(this);
        final ArrayList<Cell> neighbors = new ArrayList<>(4);
        final int rowBound = problem.getRowBound();
        final int colBound = problem.getColBound();
        if (row - 1 >= 0) {
            neighbors.add(new Cell(row - 1, col));
        }
        if (col + 1 < colBound) {
            neighbors.add(new Cell(row, col + 1));
        }
        if (row + 1 < rowBound) {
            neighbors.add(new Cell(row + 1, col));
        }
        if (col - 1 >= 0) {
            neighbors.add(new Cell(row, col - 1));
        }
        assert neighbors.stream().allMatch(problem::containsCell);
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
