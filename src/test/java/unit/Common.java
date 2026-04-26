package unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import think.common.PathTracer;
import think.common.StandardEvaluator;
import think.domain.codec.Parser;
import think.domain.codec.Parser.BadMapCodeException;
import think.domain.model.Puzzle;

public final class Common {

    /**
        Given multiple shortest paths, prefer UP > RIGHT > DOWN > LEFT. This is good for
        determinism and matching Pathery.
     */
    @Test
    public void snakePreferenceOrder() throws BadMapCodeException {
        final Puzzle puzzle1 = Parser.parse("2.2.0.BottomLeft->TopRight...:1,f1.,s1.");
        final PathTracer tracer1 = new PathTracer(puzzle1);
        final int[] result1 = tracer1.trace(puzzle1.tiles());
        Assertions.assertArrayEquals(new int[] { 1, 1, 1, 0 }, result1);

        final Puzzle puzzle2 = Parser.parse("2.2.0.TopRight->BottomLeft...:1,s1.,f1.");
        final PathTracer tracer2 = new PathTracer(puzzle2);
        final int[] result2 = tracer2.trace(puzzle2.tiles());
        Assertions.assertArrayEquals(new int[] { 0, 1, 1, 1 }, result2);
    }

    @Test
    public void traceVisitCountMatchesScorePlusOne() throws BadMapCodeException {
        final Puzzle puzzle = Parser.parse("3.2.0.Two segments...:,s1.1,c1.2,f1.");
        final int score = new StandardEvaluator(puzzle).evaluate(puzzle.tiles());
        final int[] result = new PathTracer(puzzle).trace(puzzle.tiles());
        Assertions.assertEquals(score + 1, sum(result));
    }

    private static int sum(final int[] values) {
        int total = 0;
        for (final int value : values) {
            total += value;
        }
        return total;
    }
}
