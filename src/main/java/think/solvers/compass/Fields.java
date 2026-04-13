package think.solvers.compass;

import java.util.Arrays;
import think.common.DistanceFinder;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.ints.IntArrays;

// TODO: this is the wrong abstraction.
class Fields {

    static final int NOT_ON_SHORTEST_PATH = 0;
    private final DistanceFinder finder;
    private final int puzzleSize;
    private final int[] waypoints;
    private final int[][] fieldsFromSourcesByWaypoint;
    private final int[][] fieldsBetweenSourcesByWaypoint;
    private final int[][] layerWidthsByWaypoint;
    private final int[] buffer;

    Fields(final Puzzle puzzle) {
        this.puzzleSize = puzzle.getNumRows() * puzzle.getNumCols();
        this.finder = new DistanceFinder(puzzle);
        this.waypoints = puzzle.getWaypoints();
        this.fieldsFromSourcesByWaypoint = new int[waypoints.length][puzzleSize];
        this.fieldsBetweenSourcesByWaypoint = new int[waypoints.length - 1][puzzleSize];
        this.layerWidthsByWaypoint = new int[waypoints.length - 1][puzzleSize];
        this.buffer = new int[puzzleSize];
    }

    void adapt(final Tile[] state) {
        // The distance of each cell from each source is exactly what DistanceFinder says it is.
        for (int index = 0; index < waypoints.length; index += 1) {
            finder.find(fieldsFromSourcesByWaypoint[index], state, waypoints[index]);
        }
        // The sum of fields between two waypoints (assuming they are connected) is at a minimum
        // when a cell is in the shortest path untion. That minimum is the distance between
        // waypoints. Any increase of `i` over this minimum means if you got here, you need to take
        // `i` more steps than necessary to get to the finish.
        for (int waypointIndex = 0; waypointIndex < waypoints.length - 1; waypointIndex += 1) {
            final int[] startField = fieldsFromSourcesByWaypoint[waypointIndex];
            final int[] finishField = fieldsFromSourcesByWaypoint[waypointIndex + 1];
            final int[] betweenField = fieldsBetweenSourcesByWaypoint[waypointIndex];
            for (int tileIndex = 0; tileIndex < puzzleSize; tileIndex += 1) {
                betweenField[tileIndex] = startField[tileIndex] + finishField[tileIndex];
            }
        }
        // Since the maze is unweighted (equivalently, has all edges have the same weight), we can
        // calculate the number of cells at a given distance from the start waypoint ("depth") that
        // lie on any shortest path. Store this in the buffer. Knowing how to turn a tileIndex into
        // a depth and a depth into a layerWidth, we can compute the width each cell lies on.
        for (int waypointIndex = 0; waypointIndex < waypoints.length - 1; waypointIndex += 1) {
            final int[] fromStartField = fieldsFromSourcesByWaypoint[waypointIndex];
            final int[] betweenField = fieldsBetweenSourcesByWaypoint[waypointIndex];
            final int minDistanceFromBoth = betweenField[waypoints[waypointIndex]];
            Arrays.fill(buffer, NOT_ON_SHORTEST_PATH);
            for (int tileIndex = 0; tileIndex < puzzleSize; tileIndex += 1) {
                final int distanceFromBoth = betweenField[tileIndex];
                final boolean isOnShortestPath = distanceFromBoth == minDistanceFromBoth;
                if (isOnShortestPath) {
                    final int depth = fromStartField[tileIndex];
                    buffer[depth] += 1;
                }
            }
            final int[] layerWidth = layerWidthsByWaypoint[waypointIndex];
            for (int tileIndex = 0; tileIndex < puzzleSize; tileIndex += 1) {
                final int depth = fromStartField[tileIndex];
                layerWidth[tileIndex] = buffer[depth];
            }
        }
    }

    int[] fieldViewAt(final int waypointIndex) {
        return fieldsFromSourcesByWaypoint[waypointIndex];
    }

    int[] fieldViewAfter(final int waypointIndex) {
        return fieldsBetweenSourcesByWaypoint[waypointIndex];
    }

    int[] layerWidthViewAfter(final int waypointIndex) {
        return layerWidthsByWaypoint[waypointIndex];
    }

    int[] computeChokepointUnion() {
        // If only `k` cells at a given distance lie on the shortest path union, then a barrier of
        // size `k` can be used to block all shortest paths, which might cause a detour or
        // disconnect the waypoints. We call cells with the special case of `k = 1` "chokepoints".
        final int chokepointSentinel = 1;
        Arrays.fill(buffer, 0);
        for (int waypointIndex = 0; waypointIndex < waypoints.length - 1; waypointIndex += 1) {
            final int[] layerWidth = layerWidthsByWaypoint[waypointIndex];
            for (int tileIndex = 0; tileIndex < puzzleSize; tileIndex += 1) {
                if (layerWidth[tileIndex] == 1) {
                    buffer[tileIndex] = chokepointSentinel;
                }
            }
        }
        return IntArrays.ofRangeWhere(0, puzzleSize, index -> buffer[index] == chokepointSentinel);
    }
}
