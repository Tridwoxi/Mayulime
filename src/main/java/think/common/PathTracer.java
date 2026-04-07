package think.common;

import java.util.Arrays;
import java.util.BitSet;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.ints.IntQueue;

/**
    Find what cells are visited by a shortest path through consecutive checkpoints. Path obeys UP,
    RIGHT, DOWN, LEFT snake preference order. If checkpoints are disconnected, the resulting set
    will be empty.
 */
public final class PathTracer {

    private final int[] checkpoints;
    private final int numRows;
    private final int numCols;
    private final int size;
    private final IntQueue frontier;
    private final int[] distances;

    public PathTracer(final Puzzle puzzle) {
        this.checkpoints = puzzle.getCheckpoints();
        this.numRows = puzzle.getNumRows();
        this.numCols = puzzle.getNumCols();
        this.size = numRows * numCols;
        this.frontier = new IntQueue(size);
        this.distances = new int[size];
    }

    public BitSet trace(final Feature[] features) {
        if (features.length != size) {
            throw new IllegalArgumentException();
        }
        final BitSet visited = new BitSet(size);
        for (int seg = 0; seg < checkpoints.length - 1; seg += 1) {
            final int start = checkpoints[seg];
            final int finish = checkpoints[seg + 1];
            if (!traceSegment(features, start, finish, visited)) {
                return new BitSet(size);
            }
        }
        return visited;
    }

    private boolean traceSegment(
        final Feature[] features,
        final int start,
        final int finish,
        final BitSet visited
    ) {
        if (features[start].isBlocked() || features[finish].isBlocked()) {
            return false;
        }
        bfsFrom(features, finish, start);
        if (distances[start] < 0) {
            return false;
        }

        int current = start;
        visited.set(current);
        while (current != finish) {
            final int currentRow = current / numCols;
            final int currentCol = current % numCols;
            final int needed = distances[current] - 1;

            final int nextUp = current - numCols;
            final int nextRight = current + 1;
            final int nextDown = current + numCols;
            final int nextLeft = current - 1;

            if (currentRow > 0 && distances[nextUp] == needed) {
                current = nextUp;
            } else if (currentCol < numCols - 1 && distances[nextRight] == needed) {
                current = nextRight;
            } else if (currentRow < numRows - 1 && distances[nextDown] == needed) {
                current = nextDown;
            } else if (currentCol > 0 && distances[nextLeft] == needed) {
                current = nextLeft;
            } else {
                return false;
            }
            visited.set(current);
        }
        return true;
    }

    private void bfsFrom(final Feature[] features, final int source, final int target) {
        Arrays.fill(distances, -1);
        frontier.clear();
        distances[source] = 0;
        frontier.add(source);

        while (!frontier.isEmpty()) {
            final int current = frontier.remove();

            final int currentRow = current / numCols;
            final int currentCol = current % numCols;
            final int nextDistance = distances[current] + 1;

            final int nextUp = current - numCols;
            final int nextRight = current + 1;
            final int nextDown = current + numCols;
            final int nextLeft = current - 1;

            if (currentRow > 0 && features[nextUp].isPassable() && distances[nextUp] < 0) {
                if (nextUp == target) {
                    distances[nextUp] = nextDistance;
                    return;
                }
                frontier.add(nextUp);
                distances[nextUp] = nextDistance;
            }
            if (
                currentCol < numCols - 1 &&
                features[nextRight].isPassable() &&
                distances[nextRight] < 0
            ) {
                if (nextRight == target) {
                    distances[nextRight] = nextDistance;
                    return;
                }
                frontier.add(nextRight);
                distances[nextRight] = nextDistance;
            }
            if (
                currentRow < numRows - 1 &&
                features[nextDown].isPassable() &&
                distances[nextDown] < 0
            ) {
                if (nextDown == target) {
                    distances[nextDown] = nextDistance;
                    return;
                }
                frontier.add(nextDown);
                distances[nextDown] = nextDistance;
            }
            if (currentCol > 0 && features[nextLeft].isPassable() && distances[nextLeft] < 0) {
                if (nextLeft == target) {
                    distances[nextLeft] = nextDistance;
                    return;
                }
                frontier.add(nextLeft);
                distances[nextLeft] = nextDistance;
            }
        }
    }
}
