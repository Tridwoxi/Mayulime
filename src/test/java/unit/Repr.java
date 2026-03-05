package unit;

import domain.codec.Parser;
import domain.codec.Parser.BadMapCodeException;
import domain.model.Maze;
import domain.model.Maze.Feature;
import domain.model.Puzzle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class Repr {

    private static final String SMALL1_MAPCODE = """
        13.6.7.Small1...:,r3.11,r3.,r3.2,r1.8,r3.,r3.8,r1.2,f1.,s1.11,r3.,r3.2,r1.,r1.4,r1.2,r3.,r3.2,c1.8,r3.""";

    @Test
    public void puzzleFaithfulToMapCode() throws BadMapCodeException {
        final Puzzle puzzle = Parser.parse(SMALL1_MAPCODE);
        final Maze maze = puzzle.getMaze();
        final Feature[] grid = maze.getGrid();

        int blankCount = 0;
        int systemWallCount = 0;
        int checkpointCount = 0;
        int playerWallCount = 0;
        for (final Feature feature : grid) {
            switch (feature) {
                case BLANK -> blankCount += 1;
                case SYSTEM_WALL -> systemWallCount += 1;
                case CHECKPOINT -> checkpointCount += 1;
                case PLAYER_WALL -> playerWallCount += 1;
                default -> throw new AssertionError();
            }
        }

        Assertions.assertEquals("Small1", puzzle.getName());
        Assertions.assertEquals(6, maze.getNumRows());
        Assertions.assertEquals(13, maze.getNumCols());
        Assertions.assertEquals(7, puzzle.getBlockingBudget());
        Assertions.assertEquals(78, grid.length);
        Assertions.assertEquals(60, blankCount);
        Assertions.assertEquals(15, systemWallCount);
        Assertions.assertEquals(3, checkpointCount);
        Assertions.assertEquals(0, playerWallCount);

        final int[] checkpoints = puzzle.getCheckpoints();
        Assertions.assertEquals(3, checkpoints.length);
        Assertions.assertEquals(5 * 13 + 3, checkpoints[1]);
        Assertions.assertEquals(Feature.CHECKPOINT, maze.getFeature(checkpoints[0]));
        Assertions.assertEquals(Feature.CHECKPOINT, maze.getFeature(checkpoints[1]));
        Assertions.assertEquals(Feature.CHECKPOINT, maze.getFeature(checkpoints[2]));
    }
}
