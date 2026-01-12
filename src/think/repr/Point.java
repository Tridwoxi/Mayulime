package think.repr;

import java.util.ArrayList;

/**
    A lightweight (i, j) index pair used as grid.get(i).get(j). For JavaFX, i is Y, and
    j is X.
 */
public record Point(int i, int j) {
    public ArrayList<Point> getNeighbors(final Problem board) {
        // Potential optimization: get Point instances from board to reuse them,
        // reducing GC pressure. Reasonable because this method is important to BFS.
        final ArrayList<Point> neighbors = new ArrayList<>(4);
        final int boundI = board.getBoundI();
        final int boundJ = board.getBoundJ();
        if (i - 1 >= 0) {
            neighbors.add(new Point(i - 1, j));
        }
        if (j + 1 < boundJ) {
            neighbors.add(new Point(i, j + 1));
        }
        if (i + 1 < boundI) {
            neighbors.add(new Point(i + 1, j));
        }
        if (j - 1 >= 0) {
            neighbors.add(new Point(i, j - 1));
        }
        assert neighbors.size() >= 2 && neighbors.size() <= 4;
        return neighbors;
    }

    /**
        Two points are neighbors iff they are adjacent horizontally or vertically (Von
        Neumann neighborhood). Points are not neighbors of themselves. Neighbor-ness is
        symmetric. Assumes but does not verify that points are on the same board.
     */
    public boolean isNeighbor(final Point other) {
        return manhattan(other) == 1;
    }

    public int manhattan(final Point other) {
        return Math.abs(this.i - other.i) + Math.abs(this.j - other.j);
    }
}
