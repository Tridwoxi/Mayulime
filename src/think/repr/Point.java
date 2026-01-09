package think.repr;

import java.util.ArrayList;

/**
    A lightweight (i, j) index pair used as array[i][j]. For JavaFX, i is Y, and j is X.
 */
public record Point(int i, int j) {
    public ArrayList<Point> getNeighbors(final Problem board) {
        final ArrayList<Point> neighbors = new ArrayList<>(4);
        if (i - 1 >= 0) {
            neighbors.add(new Point(i - 1, j));
        }
        if (j + 1 < board.getBoundJ()) {
            neighbors.add(new Point(i, j + 1));
        }
        if (i + 1 < board.getBoundI()) {
            neighbors.add(new Point(i + 1, j));
        }
        if (j - 1 >= 0) {
            neighbors.add(new Point(i, j - 1));
        }
        return neighbors;
    }
}
