package think.common;

import think.domain.model.Feature;

/**
    Answers the question "How far is this cell from all other cells?" efficiently. A distance of -1
    indicates that cell is unreachable from the source. Blocked cells are unreachable. If the
    source is blocked, all cells are unreachable.
 */
public final class DistanceFinder {

    private static final int UNREACHABLE = -1;

    private DistanceFinder() {}

    public static int[] find(
        final Feature[] features,
        final int numRows,
        final int numCols,
        final int source
    ) {
        final boolean sourceInBounds = source >= 0 && source < features.length;
        final boolean dimensionsOkay = features.length == numRows * numCols && numCols > 0;
        if (!sourceInBounds || !dimensionsOkay) {
            throw new IllegalArgumentException();
        }
        final int[] distances = IntArrays.ofConstant(UNREACHABLE, features.length);
        if (features[source].isBlocked()) {
            return distances;
        }
        final IntDeque frontier = new IntDeque(distances.length);
        distances[source] = 0;
        frontier.addLast(source);
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
                frontier.addLast(nextUp);
                distances[nextUp] = nextDistance;
            }
            if (
                currentCol < numCols - 1 &&
                features[nextRight].isPassable() &&
                distances[nextRight] < 0
            ) {
                frontier.addLast(nextRight);
                distances[nextRight] = nextDistance;
            }
            if (
                currentRow < numRows - 1 &&
                features[nextDown].isPassable() &&
                distances[nextDown] < 0
            ) {
                frontier.addLast(nextDown);
                distances[nextDown] = nextDistance;
            }
            if (currentCol > 0 && features[nextLeft].isPassable() && distances[nextLeft] < 0) {
                frontier.addLast(nextLeft);
                distances[nextLeft] = nextDistance;
            }
        }
        return distances;
    }
}
