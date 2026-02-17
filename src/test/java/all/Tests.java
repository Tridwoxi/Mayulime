package all;

import infra.io.Logging;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import think.ana.Snake;
import think.repr.Grid;
import think.repr.Grid.Cell;
import think.repr.Problem;
import think.repr.Problem.BadMapCodeException;
import think.repr.Problem.Feature;
import think.tools.Random;
import think.tools.Structures.Weighted;

/**
    Testing of the system is done by unit tests (below) and assertions in the code.
    Assertions are used for both conditions and invariants. Tests are used for
    end-to-end behavior and running the assertions. Do not write tests that are already
    covered by assertions.

    We keep all tests in this single file because there aren't many things to test.
 */
public final class Tests {

    private static final String EXAMPLE = """
        6.4.13.Example...:7,c2.,r1.,u2.,t1.,r1.,u1.,t2.,r1.3,s1.1,r1.,c1.1,f1.
        """;

    private Tests() {}

    /**
        Death by failed assertion if any test fails.

        This method always returns true. The preferred way to run this method is to
        assert it upon system launch.
     */
    public static boolean runAllTests() {
        Logging.log(Tests.class, "Running unit tests");
        problemParsing();
        snakePathfinding();
        snakeTiebreaking();
        solutionValidation();
        weightedSelection();
        return true;
    }

    // == think.ana ====================================================================

    private static void snakePathfinding() {
        final Problem problem = getProblem();
        assert Snake.evaluate(problem, problem.getCachedInitial()) == 30;
        final Grid<Feature> solution = problem.getAnotherInitial();
        solution.set(new Cell(0, 3), Feature.PLAYER_WALL);
        assert Snake.evaluate(problem, solution) == 0;
    }

    private static void snakeTiebreaking() {
        final BiFunction<Cell, Cell, Cell> first2x2 = (start, end) -> {
            final Optional<ArrayList<Cell>> steps = Snake.travel(
                new Grid<>(Feature.EMPTY, 2, 2),
                start,
                end,
                new HashSet<>(),
                new HashMap<>()
            );
            return steps.orElseThrow().getFirst();
        };
        final Cell topLeft = new Cell(0, 0);
        final Cell bottomRight = new Cell(1, 1);
        final Cell topRight = new Cell(0, 1);
        final Cell bottomLeft = new Cell(1, 0);
        assert first2x2.apply(topLeft, bottomRight).equals(topRight);
        assert first2x2.apply(bottomLeft, topRight).equals(topLeft);
        assert first2x2.apply(bottomRight, topLeft).equals(topRight);
        assert first2x2.apply(topRight, bottomLeft).equals(bottomRight);
    }

    // == think.repr ===================================================================

    private static void problemParsing() {
        final Problem problem = getProblem();

        final Grid<Feature> initial = problem.getCachedInitial();
        assert initial.getNumRows() == 4;
        assert initial.getNumCols() == 6;
        assert problem.getPlayerWallSupply() == 12;
        assert problem.getName().equals("Example");

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

    private static void solutionValidation() {
        final Problem problem = getProblem();
        final Grid<Feature> solution = problem.getAnotherInitial();

        assert problem.isValid(solution);

        solution.set(new Cell(0, 0), Feature.SYSTEM_WALL);
        assert !problem.isValid(solution);
        solution.set(new Cell(0, 0), Feature.EMPTY);

        solution.set(new Cell(3, 3), Feature.TELEPORT_OUT);
        assert !problem.isValid(solution);
        solution.set(new Cell(3, 3), Feature.CHECKPOINT);

        solution.set(new Cell(0, 2), Feature.PLAYER_WALL);
        assert problem.isValid(solution);
        solution.set(new Cell(3, 3), Feature.EMPTY);
    }

    // == think.tools ==================================================================

    private static void weightedSelection() {
        final List<Weighted<String>> weighteds = List.of(
            new Weighted<>("Light", 1e-10),
            new Weighted<>("Heavy", 1e+10),
            new Weighted<>("Light", 1e-10)
        );
        for (int attempt = 0; attempt < 10; attempt += 1) {
            assert Random.weightedStream(new ArrayList<>(weighteds))
                .findFirst()
                .orElseThrow()
                .equals("Heavy");
        }
    }

    // == Helpers. =====================================================================

    private static Problem getProblem() {
        try {
            return new Problem(EXAMPLE);
        } catch (BadMapCodeException exception) {
            throw new AssertionError();
        }
    }
}
