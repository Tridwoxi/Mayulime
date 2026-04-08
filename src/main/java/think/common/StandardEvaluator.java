package think.common;

import java.util.Arrays;
import think.domain.model.Feature;
import think.domain.model.Puzzle;
import think.ints.IntQueue;

/**
    Simple reference evaluator implementation. Calculates sum of pairwise distances between
    checkpoints, or NO_PATH_EXISTS if checkpoints are disconnected. Not thread-safe.
 */
public final class StandardEvaluator {

    public static final int NO_PATH_EXISTS = -1;
    private final int numRows;
    private final int numCols;
    private final int size;
    private final IntQueue frontier;
    private final int[] distances;
    private final int[] checkpoints;

    public StandardEvaluator(final Puzzle puzzle) {
        this.numRows = puzzle.getNumRows();
        this.numCols = puzzle.getNumCols();
        this.size = numRows * numCols;
        this.frontier = new IntQueue(size);
        this.distances = new int[size];
        this.checkpoints = puzzle.getCheckpoints();
    }

    public int evaluate(final Feature[] features) {
        if (features.length != size) {
            throw new IllegalArgumentException();
        }
        int score = 0;
        for (int start = 0; start < checkpoints.length - 1; start += 1) {
            final int segmentDistance = findSegmentDistance(
                features,
                checkpoints[start],
                checkpoints[start + 1]
            );
            if (segmentDistance == NO_PATH_EXISTS) {
                return NO_PATH_EXISTS;
            }
            score += segmentDistance;
        }
        return score;
    }

    private int findSegmentDistance(final Feature[] features, final int start, final int finish) {
        if (features[start].isBlocked() || features[finish].isBlocked()) {
            return NO_PATH_EXISTS;
        }

        Arrays.fill(distances, -1);
        distances[start] = 0;
        frontier.clear();
        frontier.add(start);

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

            // "If passable and unseen" is 8% faster than "If unseen and passable".
            if (currentRow > 0 && features[nextUp].isPassable() && distances[nextUp] < 0) {
                if (nextUp == finish) {
                    return nextDistance;
                }
                frontier.add(nextUp);
                distances[nextUp] = nextDistance;
            }
            if (
                currentCol < numCols - 1 &&
                features[nextRight].isPassable() &&
                distances[nextRight] < 0
            ) {
                if (nextRight == finish) {
                    return nextDistance;
                }
                frontier.add(nextRight);
                distances[nextRight] = nextDistance;
            }
            if (
                currentRow < numRows - 1 &&
                features[nextDown].isPassable() &&
                distances[nextDown] < 0
            ) {
                if (nextDown == finish) {
                    return nextDistance;
                }
                frontier.add(nextDown);
                distances[nextDown] = nextDistance;
            }
            if (currentCol > 0 && features[nextLeft].isPassable() && distances[nextLeft] < 0) {
                if (nextLeft == finish) {
                    return nextDistance;
                }
                frontier.add(nextLeft);
                distances[nextLeft] = nextDistance;
            }
        }
        return NO_PATH_EXISTS;
    }
}
