package unit;

import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import think.domain.codec.Parser;
import think.domain.codec.Parser.BadMapCodeException;
import think.domain.model.Puzzle;
import think.domain.model.Tile;

public final class Repr {

    private static final String SMALL1_MAPCODE = """
        13.6.7.Small1...:,r3.11,r3.,r3.2,r1.8,r3.,r3.8,r1.2,f1.,s1.11,r3.,r3.2,r1.,r1.4,r1.2,r3.,r3.2,c1.8,r3.""";
    private static final String MANY_WALLS_MAPCODE = """
        3.3.10.Unlimited blocking budget...:,s1.,f1.
        """;
    private static final String PARTIAL_FILL_MAPCODE = """
        3.3.2.Partial fill...:,s1.,r2.,f1.
        """;

    @Test
    public void puzzleFaithfulToMapCode() throws BadMapCodeException {
        final Puzzle puzzle = Parser.parse(SMALL1_MAPCODE);
        final Tile[] maze = puzzle.getTiles();

        int blankCount = 0;
        int systemWallCount = 0;
        int waypointCount = 0;
        int playerWallCount = 0;
        for (final Tile tile : maze) {
            switch (tile) {
                case BLANK -> blankCount += 1;
                case SYSTEM_WALL -> systemWallCount += 1;
                case WAYPOINT -> waypointCount += 1;
                case PLAYER_WALL -> playerWallCount += 1;
                default -> throw new AssertionError();
            }
        }

        Assertions.assertEquals("Small1", puzzle.getName());
        Assertions.assertEquals(6, puzzle.getNumRows());
        Assertions.assertEquals(13, puzzle.getNumCols());
        Assertions.assertEquals(7, puzzle.getBlockingBudget());
        Assertions.assertEquals(78, maze.length);
        Assertions.assertEquals(60, blankCount);
        Assertions.assertEquals(15, systemWallCount);
        Assertions.assertEquals(3, waypointCount);
        Assertions.assertEquals(0, playerWallCount);

        final int[] waypoints = puzzle.getWaypoints();
        Assertions.assertEquals(3, waypoints.length);
        Assertions.assertEquals(5 * 13 + 3, waypoints[1]);
        Assertions.assertEquals(Tile.WAYPOINT, maze[waypoints[0]]);
        Assertions.assertEquals(Tile.WAYPOINT, maze[waypoints[1]]);
        Assertions.assertEquals(Tile.WAYPOINT, maze[waypoints[2]]);
    }

    @Test
    public void manyWalls() throws BadMapCodeException {
        final Puzzle puzzle = Parser.parse(MANY_WALLS_MAPCODE);
        Assertions.assertEquals(7, puzzle.getBlockingBudget());
    }

    @Test
    public void playerWallsAreLockedAndSpent() throws BadMapCodeException {
        final Puzzle puzzle = Parser.parse(PARTIAL_FILL_MAPCODE);
        final Tile[] maze = puzzle.getTiles();
        final int system = (int) Arrays.stream(maze).filter(Tile.SYSTEM_WALL::equals).count();
        final int player = (int) Arrays.stream(maze).filter(Tile.PLAYER_WALL::equals).count();
        Assertions.assertEquals(1, system);
        Assertions.assertEquals(0, player);
        Assertions.assertEquals(Tile.SYSTEM_WALL, maze[1]);
        Assertions.assertEquals(1, puzzle.getBlockingBudget());
    }
}
