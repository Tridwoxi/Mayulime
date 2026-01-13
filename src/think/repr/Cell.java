package think.repr;

import java.util.ArrayList;

/**
    A lightweight (i, j) index pair used as grid.get(i).get(j). For JavaFX, i is Y, and
    j is X.
 */
public record Cell(int i, int j) {
    public ArrayList<Cell> getNeighbors(final Problem problem) {
        // Potential optimization: get Cell instances from problem to reuse them,
        // reducing GC pressure. Reasonable because this method is important to BFS.
        final ArrayList<Cell> neighbors = new ArrayList<>(4);
        final int boundI = problem.getBoundI();
        final int boundJ = problem.getBoundJ();
        if (i - 1 >= 0) {
            neighbors.add(new Cell(i - 1, j));
        }
        if (j + 1 < boundJ) {
            neighbors.add(new Cell(i, j + 1));
        }
        if (i + 1 < boundI) {
            neighbors.add(new Cell(i + 1, j));
        }
        if (j - 1 >= 0) {
            neighbors.add(new Cell(i, j - 1));
        }
        return neighbors;
    }

    /**
        Two cells are neighbors iff they are adjacent horizontally or vertically (Von
        Neumann neighborhood). Cells are not neighbors of themselves. Neighbor-ness is
        symmetric. Assumes but does not verify that cells are on the same problem.
     */
    public boolean isNeighbor(final Cell other) {
        return manhattan(other) == 1;
    }

    public int manhattan(final Cell other) {
        return Math.abs(this.i - other.i) + Math.abs(this.j - other.j);
    }
}
