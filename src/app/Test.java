package app;

import java.util.ArrayList;
import java.util.HashMap;
import think.ana.Pathfind;
import think.repr.Cell;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.BadMapCodeException;
import think.repr.Problem.Feature;

/**
    Testing of the system is done by unit tests (below) and assertions in the code.
    Assertions are used for both conditions and invariants. Tests are used for
    end-to-end behavior and running the assertions. Do not write tests that are already
    covered by assertions.

    We use a test file in the src/ directory because there aren't many things to test,
    and because I can't get my language server and JUnit to be friends. Regrettably,
    this couples the test running to the system running.
 */
@SuppressWarnings("checkstyle:MagicNumber")
final class Test {

    private static final String EXAMPLE = """
        6.4.13.Example...:7,c2.,r1.,u2.,t1.,r1.,u1.,t2.,r1.3,s1.1,r1.,c1.1,f1.
        """;

    private Test() {}

    /**
        Death by failed assertion if any test fails. This method always returns true.
     */
    static boolean runAllTests() {
        problemParsing();
        snakePathfinding();
        return true;
    }

    private static void problemParsing() {
        final Problem problem = getProblem();

        final Grid<Feature> initial = problem.getCachedInitial();
        assert initial.getNumRows() == 4;
        assert initial.getNumCols() == 6;
        assert problem.getPlayerWallSupply() == 13;

        final ArrayList<Cell> checkpoints = problem.getCheckpoints();
        assert checkpoints.size() == 4;
        assert checkpoints.get(0).equals(new Cell(3, 0));
        assert checkpoints.get(1).equals(new Cell(3, 3));
        assert checkpoints.get(2).equals(new Cell(1, 1));
        assert checkpoints.get(3).equals(new Cell(3, 5));

        final HashMap<Cell, Cell> teleports = problem.getTeleports();
        assert teleports.size() == 2;
        assert teleports.get(new Cell(1, 4)).equals(new Cell(2, 0));
        assert teleports.get(new Cell(2, 1)).equals(new Cell(1, 3));
    }

    private static void snakePathfinding() {
        final Problem problem = getProblem();
        final Grid<Feature> initial = problem.getCachedInitial();
        assert problem.isValid(initial);
        // FATAL ERROR: Snake pathfinding failed, says 25, Pathery says 30.
        assert Pathfind.evaluate(problem, initial) == 30;
    }

    private static Problem getProblem() {
        try {
            return new Problem(EXAMPLE);
        } catch (BadMapCodeException exception) {
            throw new AssertionError();
        }
    }
}
