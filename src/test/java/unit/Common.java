package unit;

import java.util.BitSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import think.common.PathTracer;
import think.domain.codec.Parser;
import think.domain.codec.Parser.BadMapCodeException;
import think.domain.model.Puzzle;

public final class Common {

    /**
        Given multiple shortest paths, prefer UP > RIGHT > DOWN > LEFT. This is good for
        determinism and matching Pathery.
     */
    @Test
    public void arcUpwards() throws BadMapCodeException {
        final Puzzle puzzle1 = Parser.parse("2.2.0.BottomLeft->TopRight...:1,f1.,s1.");
        final PathTracer tracer1 = new PathTracer(puzzle1);
        final BitSet result1 = tracer1.trace(puzzle1.getFeatures());
        final BitSet expected1 = new BitSet(4);
        expected1.set(0);
        expected1.set(1);
        expected1.set(2);
        Assertions.assertEquals(expected1, result1);

        final Puzzle puzzle2 = Parser.parse("2.2.0.TopRight->BottomLeft...:1,s1.,f1.");
        final PathTracer tracer2 = new PathTracer(puzzle2);
        final BitSet result2 = tracer2.trace(puzzle2.getFeatures());
        final BitSet expected2 = new BitSet(4);
        expected2.set(1);
        expected2.set(2);
        expected2.set(3);
        Assertions.assertEquals(expected2, result2);
    }
}
