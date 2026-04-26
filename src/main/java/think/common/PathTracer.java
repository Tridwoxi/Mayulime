package think.common;

import java.util.Arrays;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.ints.IntQueue;

/**
    Find what cells are visited by a shortest path through consecutive waypoints. Path obeys UP,
    RIGHT, DOWN, LEFT snake preference order. If waypoints are disconnected, the resulting counts
    will all be zero.
 */
public final class PathTracer {

    private final int[] waypoints;
    private final int numRows;
    private final int numCols;
    private final int size;
    private final IntQueue frontier;
    private final int[] distances;

    public PathTracer(final Puzzle puzzle) {
        this.waypoints = puzzle.waypoints();
        this.numRows = puzzle.numRows();
        this.numCols = puzzle.numCols();
        this.size = numRows * numCols;
        this.frontier = new IntQueue(size);
        this.distances = new int[size];
    }

    public int[] trace(final Tile[] state) {
        if (state.length != size) {
            throw new IllegalArgumentException();
        }
        final int[] visited = new int[size];
        visited[waypoints[0]] += 1;
        for (int segment = 0; segment < waypoints.length - 1; segment += 1) {
            final int start = waypoints[segment];
            final int finish = waypoints[segment + 1];
            if (!traceSegment(state, start, finish, visited)) {
                return new int[size];
            }
        }
        return visited;
    }

    private boolean traceSegment(
        final Tile[] state,
        final int start,
        final int finish,
        final int[] visited
    ) {
        if (state[start].isBlocked() || state[finish].isBlocked()) {
            return false;
        }
        bfsFrom(state, finish, start);
        if (distances[start] < 0) {
            return false;
        }

        int current = start;
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
            visited[current] += 1;
        }
        return true;
    }

    private void bfsFrom(final Tile[] state, final int source, final int target) {
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

            if (currentRow > 0 && state[nextUp].isPassable() && distances[nextUp] < 0) {
                if (nextUp == target) {
                    distances[nextUp] = nextDistance;
                    return;
                }
                frontier.add(nextUp);
                distances[nextUp] = nextDistance;
            }
            if (
                currentCol < numCols - 1 &&
                state[nextRight].isPassable() &&
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
                currentRow < numRows - 1 && state[nextDown].isPassable() && distances[nextDown] < 0
            ) {
                if (nextDown == target) {
                    distances[nextDown] = nextDistance;
                    return;
                }
                frontier.add(nextDown);
                distances[nextDown] = nextDistance;
            }
            if (currentCol > 0 && state[nextLeft].isPassable() && distances[nextLeft] < 0) {
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
