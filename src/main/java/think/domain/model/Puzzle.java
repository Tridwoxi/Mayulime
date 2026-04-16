package think.domain.model;

import infra.logging.Logger;

/**
    Pathery puzzle metadata and immutable initial maze.
 */
public record Puzzle(
    String name,
    Tile[] tiles,
    int numRows,
    int numCols,
    int[] waypoints,
    int blockingBudget
) {
    public Puzzle {
        // Error checking is hard without the parsing data, so we pray the caller is right instead.
        tiles = tiles.clone();
        waypoints = waypoints.clone();
    }

    public Tile[] tiles() {
        return tiles.clone();
    }

    public int[] waypoints() {
        return waypoints.clone();
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
