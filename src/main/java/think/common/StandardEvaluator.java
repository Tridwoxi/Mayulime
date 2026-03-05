package think.common;

import java.util.ArrayDeque;
import java.util.Arrays;
import think.domain.model.Maze;
import think.domain.model.Maze.Feature;
import think.domain.model.Puzzle;

/**
    Simple stateless reference evaluator implementation. Computes sum of pairwise distances between
    checkpoints, or any negative number if no path exists. Solvers are encouraged to write their own
    specialized implementations, but may use this one if it is good enough.
 */
public final class StandardEvaluator {

    private static final int UNREACHABLE = -1;

    private StandardEvaluator() {}

    public static int evaluate(final Puzzle puzzle, final Maze maze) {
        final int[] checkpoints = puzzle.getCheckpoints();
        final Feature[] features = maze.getGrid();

        int score = 0;
        for (int start = 0; start < checkpoints.length - 1; start += 1) {
            final int segmentDistance = findSegmentDistance(
                features,
                maze.getNumRows(),
                maze.getNumCols(),
                checkpoints[start],
                checkpoints[start + 1]
            );
            if (segmentDistance < 0) {
                return UNREACHABLE;
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
            return UNREACHABLE;
        }

        final int[] distances = new int[features.length];
        final ArrayDeque<Integer> frontier = new ArrayDeque<>(distances.length);
        Arrays.fill(distances, UNREACHABLE);
        distances[start] = 0;
        frontier.add(start);

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
        return UNREACHABLE;
    }
}
