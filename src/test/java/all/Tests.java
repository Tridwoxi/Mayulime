package all;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import think.ana.Snake;
import think.repr.Grid;
import think.repr.Grid.Cell;
import think.repr.Problem;
import think.repr.Problem.BadMapCodeException;
import think.repr.Problem.Feature;
import think.tools.Random;
import think.tools.Structures.Weighted;

/**
    Testing of the system is done with the unit tests below.

    Currently, the system uses a mix of tests and assertions. I plan to migrate
    assertions into unit tests.
 */
public final class Tests {

    private static final String EXAMPLE = """
        6.4.13.Example...:7,c2.,r1.,u2.,t1.,r1.,u1.,t2.,r1.3,s1.1,r1.,c1.1,f1.
        """;

    // == think.ana ====================================================================

    @Test
    public void snakeEvaluation() {
        final Problem problem = getProblem();
        Assertions.assertEquals(30, Snake.evaluate(problem, problem.getCachedInitial()));
        final Grid<Feature> solution = problem.getAnotherInitial();
        solution.set(new Cell(0, 3), Feature.PLAYER_WALL);
        Assertions.assertEquals(0, Snake.evaluate(problem, solution));
    }

    @Test
    public void snakeTiebreaking() {
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
        Assertions.assertEquals(topRight, first2x2.apply(topLeft, bottomRight));
        Assertions.assertEquals(topLeft, first2x2.apply(bottomLeft, topRight));
        Assertions.assertEquals(topRight, first2x2.apply(bottomRight, topLeft));
        Assertions.assertEquals(bottomRight, first2x2.apply(topRight, bottomLeft));
    }

    // == think.repr ===================================================================

    @Test
    public void parseProblem() {
        final Problem problem = getProblem();

        final Grid<Feature> initial = problem.getCachedInitial();
        Assertions.assertEquals(4, initial.getNumRows());
        Assertions.assertEquals(6, initial.getNumCols());
        Assertions.assertEquals(12, problem.getPlayerWallSupply());
        Assertions.assertEquals("Example", problem.getName());

        final ArrayList<Cell> checkpoints = problem.getCheckpoints();
        Assertions.assertEquals(4, checkpoints.size());
        Assertions.assertEquals(new Cell(3, 0), checkpoints.get(0));
        Assertions.assertEquals(new Cell(3, 3), checkpoints.get(1));
        Assertions.assertEquals(new Cell(1, 1), checkpoints.get(2));
        Assertions.assertEquals(new Cell(3, 5), checkpoints.get(3));

        final HashMap<Cell, Cell> teleports = problem.getTeleports();
        Assertions.assertEquals(2, teleports.size());
        Assertions.assertEquals(new Cell(2, 0), teleports.get(new Cell(1, 4)));
        Assertions.assertEquals(new Cell(1, 3), teleports.get(new Cell(2, 1)));
    }

    @Test
    public void solutionValidation() {
        final Problem problem = getProblem();
        final Grid<Feature> solution = problem.getAnotherInitial();

        Assertions.assertTrue(problem.isValid(solution));

        solution.set(new Cell(0, 0), Feature.SYSTEM_WALL);
        Assertions.assertFalse(problem.isValid(solution));
        solution.set(new Cell(0, 0), Feature.EMPTY);

        solution.set(new Cell(3, 3), Feature.TELEPORT_OUT);
        Assertions.assertFalse(problem.isValid(solution));
        solution.set(new Cell(3, 3), Feature.CHECKPOINT);

        solution.set(new Cell(0, 2), Feature.PLAYER_WALL);
        Assertions.assertTrue(problem.isValid(solution));
        solution.set(new Cell(3, 3), Feature.EMPTY);
    }

    // == think.tools ==================================================================

    @Test
    public void weightedSelection() {
        final List<Weighted<String>> weighteds = List.of(
            new Weighted<>("Light", 1e-10),
            new Weighted<>("Heavy", 1e+10),
            new Weighted<>("Light", 1e-10)
        );
        for (int attempt = 0; attempt < 10; attempt += 1) {
            Assertions.assertEquals(
                "Heavy",
                Random.weightedStream(new ArrayList<>(weighteds))
                    .findFirst()
                    .orElseThrow()
            );
        }
    }

    // == Helpers. =====================================================================

    private static Problem getProblem() {
        try {
            return new Problem(EXAMPLE);
        } catch (BadMapCodeException exception) {
            Assertions.fail(exception);
            throw new AssertionError();
        }
    }
}
