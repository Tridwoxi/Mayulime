package think.domain.model;

import infra.logging.Logger;
import java.util.HashSet;

/**
    Pathery puzzle metadata and immutable initial maze.
 */
public final class Puzzle {

    private final String name;
    private final Tile[] tiles;
    private final int numRows;
    private final int numCols;
    private final int[] waypoints;
    private final int blockingBudget;

    public Puzzle(
        final String name,
        final Tile[] tiles,
        final int numRows,
        final int numCols,
        final int[] waypoints,
        final int blockingBudget
    ) {
        final HashSet<Integer> seen = new HashSet<>(waypoints.length);
        for (final int waypoint : waypoints) {
            if (waypoint < 0 || waypoint >= numRows * numCols || !seen.add(waypoint)) {
                throw new IllegalArgumentException();
            }
        }
        this.name = name;
        this.tiles = tiles.clone();
        this.numRows = numRows;
        this.numCols = numCols;
        this.waypoints = waypoints.clone();
        this.blockingBudget = blockingBudget;
    }

    public String getName() {
        return name;
    }

    public Tile[] getTiles() {
        return tiles.clone();
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public int[] getWaypoints() {
        return waypoints.clone();
    }

    public int getBlockingBudget() {
        return blockingBudget;
    }

    public boolean isValid(final Tile[] proposal) {
        if (tiles.length != proposal.length) {
            Logger.warning("Wrong dimension: %d vs %d", tiles.length, proposal.length);
            return false;
        }
        int numWalls = 0;
        for (int index = 0; index < tiles.length; index += 1) {
            final boolean unchanged = tiles[index] == proposal[index];
            final boolean wallPlaced =
                tiles[index] == Tile.BLANK && proposal[index] == Tile.PLAYER_WALL;
            if (wallPlaced) {
                numWalls += 1;
            }
            if (!(unchanged || wallPlaced)) {
                return false;
            }
        }
        return numWalls <= blockingBudget;
    }
}
