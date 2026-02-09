package think.ana;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import think.repr.Cell;
import think.repr.Grid;
import think.repr.Problem.Feature;
import think.tools.Structures.Pair;

/**
    Distance evaluation.
 */
public final class Distances {

    public enum PathDirection {
        // enum instead of boolean https://read.engineerscodex.com/p/the-boolean-trap
        SOURCE_TO_REACHABLE,
        REACHABLE_TO_SOURCE,
    }

    private Distances() {}

    /**
        Get the distance from the source cell to every other cell in the grid.

        In the resulting grid, negative numbers mean a cell is unreachable. Cells that
        are not open are always unreachable.
     */
    public static Grid<Integer> distanceFrom(
        final Grid<Feature> solution,
        final Cell source
    ) {
        assert Inspect.isOpen(solution.get(source)) && solution.inBounds(source);

        // The "cells with a negative distance are unreachable" convention was chosen
        // because it is invariant under addition of distance grids with connected
        // sources. If the sources are not connected, it may not hold.
        final int numRows = solution.getNumRows();
        final int numCols = solution.getNumCols();
        final Grid<Integer> distances = new Grid<>(-1, numRows, numCols);

        // PERF: Unbox primitives, use raw indexes instead of Cells, avoid modulo and
        // division, inline and unroll neighbor loop, reuse structures where possible.
        final ArrayDeque<Cell> frontier = new ArrayDeque<>();
        frontier.add(source);
        distances.set(source, 0);
        while (!frontier.isEmpty()) {
            final Cell current = frontier.removeFirst();
            for (final Cell neighbor : current.getNeighbors(solution)) {
                if (
                    Inspect.isOpen(solution.get(neighbor)) &&
                    distances.get(neighbor) <= -1
                ) {
                    distances.set(neighbor, distances.get(current) + 1);
                    frontier.add(neighbor);
                }
            }
        }
        assert isConsistent(distances);
        return distances;
    }

    /**
        Get the path the snake would take from some cell to another cell.

        One of the cells must be the source (distance == 0) of a distances grid. The other cell must be the reachable (distance >= 0).

        The returned path excludes the starting cell and includes the ending cell.
        Hence its size is the number of steps a snake traveling the path would take.
     */
    public static ArrayList<Cell> reconstructPath(
        final Grid<Integer> distances,
        final Cell source,
        final Cell reachable,
        final PathDirection direction
    ) {
        assert distances.get(source) == 0 && distances.get(reachable) >= 0;
        assert isConsistent(distances);
        if (source.equals(reachable)) {
            return new ArrayList<>(0);
        }
        // From the problem statement: "Among shortest paths, the Snake prefers to go
        // up, then right, then down, then left.".
        // It is unclear if URDL or LDRU is correct.
        final int length = distances.get(reachable);
        final ArrayList<Cell> path = new ArrayList<>(length);
        Cell current = reachable;
        for (int step = length; step > 0; step -= 1) {
            for (final Cell neighbor : current.getNeighborsURDL(distances)) {
                if (distances.get(neighbor) == step - 1) {
                    path.add(neighbor);
                    current = neighbor;
                    break;
                }
            }
        }
        assert current.equals(source);
        return switch (direction) {
            case REACHABLE_TO_SOURCE -> path;
            case SOURCE_TO_REACHABLE -> {
                path.removeLast();
                Collections.reverse(path);
                path.add(reachable);
                yield path;
            }
        };
    }

    private static boolean isConsistent(final Grid<Integer> distances) {
        final BiFunction<Cell, Cell, Boolean> alongEdges = (cell, neighbor) ->
            distances.get(cell) <= -1 ||
            distances.get(neighbor) <= -1 ||
            Math.abs(distances.get(cell) - distances.get(neighbor)) <= 1;
        final Predicate<Cell> acrossCells = cell ->
            cell
                .getNeighbors(distances)
                .stream()
                .allMatch(neighbor -> alongEdges.apply(cell, neighbor));
        return distances.stream().map(Pair::second).allMatch(acrossCells);
    }
}
