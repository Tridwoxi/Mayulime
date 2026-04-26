package infra.gui;

import java.util.Arrays;
import think.common.PathTracer;
import think.domain.model.Puzzle;
import think.domain.model.Tile;

/**
    Snapshot of a solver proposal prepared for GUI rendering.
 */
public final class Submission {

    private static final int NOT_A_WAYPOINT = -1;

    private final String submitter;
    private final Tile[] maze;
    private final int[] waypointOrderByIndex;
    private final int[] pathVisits;
    private final int numRows;
    private final int numCols;
    private final int score;
    private final int blockingBudget;

    public Submission(
        final String submitter,
        final Puzzle puzzle,
        final Tile[] state,
        final int score
    ) {
        this.submitter = submitter;
        this.maze = state.clone();
        this.numRows = puzzle.numRows();
        this.numCols = puzzle.numCols();
        this.score = score;
        this.blockingBudget = puzzle.blockingBudget();
        this.waypointOrderByIndex = buildWaypointOrderByIndex(puzzle, this.maze.length);
        this.pathVisits = new PathTracer(puzzle).trace(this.maze);
    }

    public String getSubmitter() {
        return submitter;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public Tile[] getState() {
        return maze.clone();
    }

    public Tile getTile(final int row, final int col) {
        return maze[row * numCols + col];
    }

    public int getWaypointOrder(final int row, final int col) {
        return waypointOrderByIndex[row * numCols + col];
    }

    public int getScore() {
        return score;
    }

    public boolean isOnPath(final int row, final int col) {
        return pathVisits[row * numCols + col] > 0;
    }

    public int getBlockingBudget() {
        return blockingBudget;
    }

    private static int[] buildWaypointOrderByIndex(final Puzzle puzzle, final int numCells) {
        final int[] orderByIndex = new int[numCells];
        Arrays.fill(orderByIndex, NOT_A_WAYPOINT);

        final int[] waypoints = puzzle.waypoints();
        for (int order = 0; order < waypoints.length; order += 1) {
            orderByIndex[waypoints[order]] = order;
        }
        return orderByIndex;
    }
}
