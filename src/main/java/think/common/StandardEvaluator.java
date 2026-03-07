package think.common;

import java.util.Arrays;
import think.domain.model.Feature;
import think.domain.model.Puzzle;

/**
    Simple stateless reference evaluator implementation. Computes sum of pairwise distances between
    checkpoints, or -1 if no path exists. Solvers are encouraged to write their own specialized
    implementations, but may use this one if it is good enough.
 */
public final class StandardEvaluator {

    private static final int NO_PATH_EXISTS = -1;

    private StandardEvaluator() {}

    public static int evaluate(final Puzzle puzzle, final Feature[] features) {
        if (puzzle.getNumRows() * puzzle.getNumCols() != features.length) {
            throw new IllegalArgumentException();
        }
        final int[] checkpoints = puzzle.getCheckpoints();
        final int numRows = puzzle.getNumRows();
        final int numCols = puzzle.getNumCols();

        int score = 0;
        for (int start = 0; start < checkpoints.length - 1; start += 1) {
            final int segmentDistance = findSegmentDistance(
                features,
                numRows,
                numCols,
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

    private static int findSegmentDistance(
        final Feature[] features,
        final int numRows,
        final int numCols,
        final int start,
        final int finish
    ) {
        if (features[start].isBlocked() || features[finish].isBlocked()) {
            return NO_PATH_EXISTS;
        }

        final int[] distances = new int[features.length];
        final IntDeque frontier = new IntDeque(distances.length);
        Arrays.fill(distances, -1);
        distances[start] = 0;
        frontier.addLast(start);

        while (!frontier.isEmpty()) {
            final int current = frontier.removeFirst();

            final int currentRow = current / numCols;
            final int currentCol = current % numCols;
            final int currentDistance = distances[current];

            final int nextUp = current - numCols;
            final int nextRight = current + 1;
            final int nextDown = current + numCols;
            final int nextLeft = current - 1;
            final int nextDistance = currentDistance + 1;

            if (currentRow > 0 && features[nextUp].isPassable() && distances[nextUp] < 0) {
                if (nextUp == finish) {
                    return nextDistance;
                }
                frontier.addLast(nextUp);
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
                frontier.addLast(nextRight);
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
                frontier.addLast(nextDown);
                distances[nextDown] = nextDistance;
            }
            if (currentCol > 0 && features[nextLeft].isPassable() && distances[nextLeft] < 0) {
                if (nextLeft == finish) {
                    return nextDistance;
                }
                frontier.addLast(nextLeft);
                distances[nextLeft] = nextDistance;
            }
        }
        return NO_PATH_EXISTS;
    }
}
