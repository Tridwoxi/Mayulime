package unit;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import think.domain.codec.Parser;
import think.domain.codec.Parser.BadMapCodeException;
import think.domain.codec.Serializer;
import think.domain.model.Puzzle;
import think.domain.model.Tile;

public final class Codec {

    private static final List<String> VALID = List.of(
        "3.3.10.Many tiles, all supported...:,s1.1,r1.,r3.2,c1.1,f1.",
        "3.2.10.Waypoints in a funny order...:,s1.,c7.,f1.,c14.,c5.,c11.",
        "3.3.0.Partial fill...:,s1.,f1."
    );

    private static final List<String> INVALID = List.of(
        "3.1.10.Multiple starts...:,s1.,s1.,f1.",
        "3.1.9.Missing finish...:,s1.",
        "4.1.0.Duplicate waypoint orders...:,s1.,c1.,c1.,f1.",
        "3.2.9.Unsupported teleport (for now)...:,s1.,t1.,f1.1,u1.", // Move when supported.
        "3.1.0.Unsupported wall order...:,s1.,r4.,f1.",
        "1001.1001.0.DOS attack...:,s1.,f1.",
        "3.2.10.Out of bounds...:,s1.1,r1.,r3.2,c1.1,f1.",
        "3.3.10.Spurious comma...:,s1.1,r1.,r3,.2,c1.1,f1.",
        "3.3.10.Spurious period...:s1.1,r1.,r3..2,c1.1,f1."
    );

    @Test
    public void truePositives() throws BadMapCodeException {
        VALID.forEach(mapCode ->
            Assertions.assertDoesNotThrow(() -> Parser.parse(mapCode), mapCode)
        );
    }

    @Test
    public void trueNegatives() throws BadMapCodeException {
        INVALID.forEach(mapCode ->
            Assertions.assertThrows(BadMapCodeException.class, () -> Parser.parse(mapCode), mapCode)
        );
    }

    @Test
    public void serializerRoundTripsPuzzle() throws BadMapCodeException {
        final Puzzle puzzle = Parser.parse(VALID.get(0));

        final String serialized = Serializer.serialize(puzzle);
        final Puzzle reparsed = Parser.parse(serialized);

        Assertions.assertEquals(puzzle.name(), reparsed.name());
        Assertions.assertEquals(puzzle.numRows(), reparsed.numRows());
        Assertions.assertEquals(puzzle.numCols(), reparsed.numCols());
        Assertions.assertEquals(puzzle.blockingBudget(), reparsed.blockingBudget());
        Assertions.assertArrayEquals(puzzle.waypoints(), reparsed.waypoints());
        Assertions.assertArrayEquals(puzzle.tiles(), reparsed.tiles());
    }

    @Test
    public void serializerIncludesPlayerWalls() throws BadMapCodeException {
        final Puzzle puzzle = Parser.parse("3.3.2.Copy state...:,s1.,f1.");
        final Tile[] state = puzzle.tiles();
        state[2] = Tile.PLAYER_WALL;
        state[7] = Tile.PLAYER_WALL;

        final String serialized = Serializer.serialize(puzzle, state);

        Assertions.assertTrue(serialized.contains(",r2."));
        Assertions.assertTrue(serialized.contains("4,r2."));
    }
}
