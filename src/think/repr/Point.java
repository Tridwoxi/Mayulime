package think.repr;

import java.util.ArrayList;

/**
    A lightweight (i, j) index pair used as array[i][j]. For JavaFX, i is Y, and j is X.
 */
public record Point(int i, int j) {
    public ArrayList<Point> getNeighbors(final Problem board) {
        final ArrayList<Point> neighbors = new ArrayList<>(4);
        final Point[] allPoints = board.getAllPoints();
        final int boundJ = board.getBoundJ();
        if (i - 1 >= 0) {
            neighbors.add(allPoints[(i - 1) * boundJ + j]);
        }
        if (j + 1 < boundJ) {
            neighbors.add(allPoints[i * boundJ + j + 1]);
        }
        if (i + 1 < board.getBoundI()) {
            neighbors.add(allPoints[(i + 1) * boundJ + j]);
        }
        if (j - 1 >= 0) {
            neighbors.add(allPoints[i * boundJ + j - 1]);
        }
        return neighbors;
    }

    /**
        Two points are neighbors iff they are adjacent horizontally or vertically (Von
        Neumann neighborhood). Points are not neighbors of themselves. Neighbor-ness is
        symmetric. Assumes but does not verify that points are on the same board.
     */
    public boolean isNeighbor(final Point other) {
        return Math.abs(this.i - other.i) + Math.abs(this.j - other.j) == 1;
    }
}
