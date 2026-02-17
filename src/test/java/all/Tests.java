package all;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import think.ana.Distances;
import think.ana.Snake;
import think.repr.Grid;
import think.repr.Grid.Cell;
import think.repr.Problem;
import think.repr.Problem.BadMapCodeException;
import think.repr.Problem.Feature;
import think.tools.Random;
import think.tools.Structures.Pair;
import think.tools.Structures.Weighted;

/**
    Testing of the system is done with the unit tests below.

    We put all the tests in one file because the project is small (projected to be no more than 5k
    loc at completion) and at such scale it is cuter to track and control everything in one place.
 */
public final class Tests {

    private static final String PROBLEM_A = """
        6.4.13.ProblemA...:7,c2.,r1.,u2.,t1.,r1.,u1.,t2.,r1.3,s1.1,r1.,c1.1,f1.
        """;
    private static final String PROBLEM_B = """
        7.5.3.ProblemB...:,u5.6,s1.4,t1.,f1.7,r1.2,t5.3,u1.,r1.3,c2.,c1.
        """;

    // == think.ana ===============================================================================

    // == Distances ==

    @Test
    public void evaluateDistances() {
        final Predicate<Grid<Integer>> isConsistent = distances -> {
            final BiFunction<Cell, Cell, Boolean> alongEdges = (cell, neighbor) ->
                distances.get(cell) <= -1 ||
                distances.get(neighbor) <= -1 ||
                Math.abs(distances.get(cell) - distances.get(neighbor)) <= 1;
            final Predicate<Cell> acrossCells = cell ->
                distances
                    .getNeighbors(cell)
                    .stream()
                    .allMatch(neighbor -> alongEdges.apply(cell, neighbor));
            return distances.stream().map(Pair::second).allMatch(acrossCells);
        };

        final Problem problemB = getProblem(PROBLEM_B);
        final Grid<Integer> distances = Distances.distanceFrom(
            problemB.getCachedInitial(),
            new Cell(2, 3)
        );
        Assertions.assertEquals(3, distances.get(new Cell(1, 1)));
        Assertions.assertTrue(distances.get(new Cell(3, 0)) < 0);
        Assertions.assertTrue(distances.get(new Cell(4, 0)) < 0);
        Assertions.assertTrue(isConsistent.test(distances));
    }

    // == Snake ==

    @Test
    public void evaluateScore() {
        final Problem problemA = getProblem(PROBLEM_A);
        final int evalInitialA = Snake.evaluate(problemA, problemA.getCachedInitial());
        Assertions.assertEquals(evalInitialA, 30);
        final Grid<Feature> solutionA = problemA.getAnotherInitial();
        solutionA.set(new Cell(0, 3), Feature.PLAYER_WALL);
        final int evalModifiedA = Snake.evaluate(problemA, solutionA);
        Assertions.assertEquals(0, evalModifiedA);

        final Problem problemB = getProblem(PROBLEM_B);
        final int evalInitialB = Snake.evaluate(problemB, problemB.getCachedInitial());
        Assertions.assertEquals(0, evalInitialB);
        final Grid<Feature> solutionB = problemB.getAnotherInitial();
        solutionB.set(new Cell(1, 4), Feature.PLAYER_WALL);
        solutionB.set(new Cell(2, 5), Feature.PLAYER_WALL);
        solutionB.set(new Cell(3, 6), Feature.PLAYER_WALL);
        final int evalModifiedB = Snake.evaluate(problemB, solutionB);
        Assertions.assertEquals(20, evalModifiedB);
    }

    @Test
    public void preferenceOrder() {
        final BiFunction<Cell, Cell, Cell> getFirstStep = (start, end) -> {
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
        Assertions.assertEquals(topRight, getFirstStep.apply(topLeft, bottomRight));
        Assertions.assertEquals(topLeft, getFirstStep.apply(bottomLeft, topRight));
        Assertions.assertEquals(topRight, getFirstStep.apply(bottomRight, topLeft));
        Assertions.assertEquals(bottomRight, getFirstStep.apply(topRight, bottomLeft));
    }

    // == think.repr ==============================================================================

    // == Problem ==

    @Test
    public void problemParsing() {
        final Problem problem = getProblem(PROBLEM_A);

        final Grid<Feature> initial = problem.getCachedInitial();
        Assertions.assertEquals(4, initial.getNumRows());
        Assertions.assertEquals(6, initial.getNumCols());
        Assertions.assertEquals(12, problem.getPlayerWallSupply());
        Assertions.assertEquals("ProblemA", problem.getName());

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
        final Problem problem = getProblem(PROBLEM_A);
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
        solution.set(new Cell(0, 2), Feature.EMPTY);
    }

    // == think.tools =============================================================================

    // == Random ==

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
                Random.weightedStream(new ArrayList<>(weighteds)).findFirst().orElseThrow()
            );
        }
    }

    // == Helpers. ================================================================================

    private static Problem getProblem(final String mapCode) {
        try {
            return new Problem(mapCode);
        } catch (BadMapCodeException exception) {
            Assertions.fail(exception);
            throw new AssertionError();
        }
    }
}
