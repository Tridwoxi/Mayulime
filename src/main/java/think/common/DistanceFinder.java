package think.common;

import java.util.Arrays;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.ints.IntQueue;

/**
    Calculates distance from every cell to a given source, or UNREACHABLE if the cell and source
    are disconnected. Blocked cells are unreachable. If the source is blocked, all cells are
    unreachable. Not thread-safe.
 */
public final class DistanceFinder {

    public static final int UNREACHABLE = -1;
    private final int numRows;
    private final int numCols;
    private final int size;
    private final IntQueue frontier;
    private final int[] distances;

    public DistanceFinder(final Puzzle puzzle) {
        this.numRows = puzzle.getNumRows();
        this.numCols = puzzle.getNumCols();
        this.size = numRows * numCols;
        this.frontier = new IntQueue(size);
        this.distances = new int[size];
    }

    public int[] find(final Feature[] features, final int source) {
        if (!(source >= 0 && source < features.length) || !(features.length == size)) {
            throw new IllegalArgumentException();
        }

        Arrays.fill(distances, UNREACHABLE);
        if (features[source].isBlocked()) {
            return distances.clone();
        }
        frontier.clear();
        distances[source] = 0;
        frontier.add(source);

        while (!frontier.isEmpty()) {
            final int current = frontier.remove();

            final int currentRow = current / numCols;
            final int currentCol = current % numCols;
            final int currentDistance = distances[current];

            final int nextUp = current - numCols;
            final int nextRight = current + 1;
            final int nextDown = current + numCols;
            final int nextLeft = current - 1;
            final int nextDistance = currentDistance + 1;

            if (currentRow > 0 && features[nextUp].isPassable() && distances[nextUp] < 0) {
                frontier.add(nextUp);
                distances[nextUp] = nextDistance;
            }
            if (
                currentCol < numCols - 1 &&
                features[nextRight].isPassable() &&
                distances[nextRight] < 0
            ) {
                frontier.add(nextRight);
                distances[nextRight] = nextDistance;
            }
            if (
                currentRow < numRows - 1 &&
                features[nextDown].isPassable() &&
                distances[nextDown] < 0
            ) {
                frontier.add(nextDown);
                distances[nextDown] = nextDistance;
            }
            if (currentCol > 0 && features[nextLeft].isPassable() && distances[nextLeft] < 0) {
                frontier.add(nextLeft);
                distances[nextLeft] = nextDistance;
            }
        }
        return distances.clone();
    }
}
