package think.ana;

import java.util.ArrayDeque;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import think.repr.Cell;
import think.repr.Grid;
import think.repr.Problem.Feature;
import think.tools.Iteration.Pair;

/**
    Distance evaluation.
 */
public final class Distances {

    private Distances() {}

    public static Grid<Integer> from(final Grid<Feature> solution, final Cell source) {
        assert Pathfind.isLegalRun(solution, source);

        // Negative numbers are represent "unreachable". Adding two distance grids with
        // connected sources maintains the invariant that unreachable cells are
        // negative. If the distance grids are not connected, the invariant does not
        // hold. Cells that are not open are marked as unreachable.
        final int numRows = solution.getNumRows();
        final int numCols = solution.getNumCols();
        final Grid<Integer> distances = new Grid<>(-1, numRows, numCols);

        // We use breadth-first search because we visit every reachable point and it
        // has slightly less overhead than A-star. "distances" also visited set.
        final ArrayDeque<Cell> frontier = new ArrayDeque<>();
        frontier.add(source);
        distances.set(source, 0);

        while (!frontier.isEmpty()) {
            final Cell current = frontier.removeFirst();
            assert distances.get(current) >= 0;
            for (final Cell neighbor : current.getNeighbors(solution)) {
                if (
                    Pathfind.isOpen(solution, neighbor) && distances.get(neighbor) <= -1
                ) {
                    distances.set(neighbor, distances.get(current) + 1);
                    frontier.add(neighbor);
                }
            }
        }
        assert isConsistent(distances, solution);
        assert distances.get(source) == 0;
        return distances;
    }

    private static boolean isConsistent(
        final Grid<Integer> distances,
        final Grid<Feature> solution
    ) {
        final BiFunction<Cell, Cell, Boolean> edgeConsistent = (cell, neighbor) ->
            distances.get(cell) <= -1 ||
            distances.get(neighbor) <= -1 ||
            Math.abs(distances.get(cell) - distances.get(neighbor)) <= 1;
        final Predicate<Cell> cellConsistent = cell ->
            cell
                .getNeighbors(solution)
                .stream()
                .allMatch(neighbor -> edgeConsistent.apply(cell, neighbor));
        return distances.stream().map(Pair::second).allMatch(cellConsistent);
    }
}
