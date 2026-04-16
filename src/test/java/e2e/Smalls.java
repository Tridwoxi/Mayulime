package e2e;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import think.domain.codec.Parser;
import think.domain.codec.Parser.BadMapCodeException;
import think.domain.model.Puzzle;
import think.manager.Manager;
import think.manager.Proposal;
import think.solvers.SolverKind;

/**
    Testing on the smalls is a good way to check the backend doesn't crash. The expected scores are
    all way less than what random guessing can achieve.
 */
public final class Smalls {

    private static final long TIMEOUT_MS = 100; // Requires about 5 MS on my machine.

    private static final String SMALL1_MAPCODE = """
        13.6.7.Small1...:,r3.11,r3.,r3.2,r1.8,r3.,r3.8,r1.2,f1.,s1.11,r3.,r3.2,r1.,r1.4,r1.2,r3.,r3.2,c1.8,r3.""";
    private static final int SMALL1_SCORE = 20; // Min score: 17; Max score: 43.

    private static final String SMALL2_MAPCODE = """
        13.6.6.Small2...:,r3.1,c1.6,r1.2,r3.,r3.4,r1.1,r1.4,r3.,r3.11,r3.,r3.11,f1.,r3.5,r1.1,r1.,r1.2,r3.,s1.11,r3.""";
    private static final int SMALL2_SCORE = 23; // Min score: 20; Max score: 40.

    private static final String SMALL3_MAPCODE = """
        13.6.7.Small3...:,r3.4,r1.4,r1.1,r3.,r3.11,r3.,r3.1,r1.9,r3.,s1.11,r3.,r3.2,r1.2,r1.2,r1.2,r3.,r3.2,c1.8,f1.""";
    private static final int SMALL3_SCORE = 17; // Min score: 14; Max score: 40.

    @Test
    public void small1() throws BadMapCodeException {
        Assertions.assertTrue(solve(SMALL1_MAPCODE, SMALL1_SCORE));
    }

    @Test
    public void small2() throws BadMapCodeException {
        Assertions.assertTrue(solve(SMALL2_MAPCODE, SMALL2_SCORE));
    }

    @Test
    public void small3() throws BadMapCodeException {
        Assertions.assertTrue(solve(SMALL3_MAPCODE, SMALL3_SCORE));
    }

    private boolean solve(final String mapCode, final int minimumRequiredScore)
        throws BadMapCodeException {
        final Puzzle puzzle = Parser.parse(mapCode);
        try (Manager manager = new Manager(SolverKind.asList())) {
            manager.solve(puzzle);
            final List<Proposal> result = manager.consumeUntil(TIMEOUT_MS);
            final int topScore = result.stream().mapToInt(Proposal::getScore).max().orElseThrow();
            return topScore >= minimumRequiredScore;
        }
    }
}
