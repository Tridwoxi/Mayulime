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

    private Distances() {}

    /**
        Get the distance from the source cell to every other cell in the grid.

        Open cells with a negative distance are unreachable. Cells that are not open are
        always unreachable.
     */
    public static Grid<Integer> distanceFrom(
        final Grid<Feature> solution,
        final Cell source
    ) {
        assert Pathfind.isLegalRun(solution, source);

        // The "cells with a negative distance are unreachable" convention was chosen
        // because it holds under addition of distance grids with connected sources. If
        // the sources are not connected, the invariant does not hold.
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
                    Pathfind.isOpen(solution, neighbor) && distances.get(neighbor) <= -1
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
        Get the path the snake would take from the source to the reachable cell.

        The returned path excludes the source and includes the given reachable cell.
        Its size is equal to the distance from the source to the reachable cell. It is
        in forward order, so the reachable cell is its last element.
     */
    public static ArrayList<Cell> reconstructPath(
        final Grid<Integer> distances,
        final Cell source,
        final Cell reachable
    ) {
        assert distances.get(source) == 0 && distances.get(reachable) >= 0;
        assert isConsistent(distances);
        if (source.equals(reachable)) {
            return new ArrayList<>(0);
        }

        // From the task specification: "Among shortest paths, the Snake prefers to go
        // up, then right, then down, then left.". From source to reachable, we explore
        // neighbors in URDL order. From reachable to source, it's LDRU.
        final int length = distances.get(reachable);
        final ArrayList<Cell> path = new ArrayList<>(length);
        Cell current = reachable;
        for (int step = length; step > 0; step -= 1) {
            for (final Cell neighbor : current.getNeighborsURDL(distances).reversed()) {
                if (distances.get(neighbor) == step - 1) {
                    path.add(neighbor);
                    current = neighbor;
                    break;
                }
            }
        }
        assert current == source;
        path.removeLast();
        Collections.reverse(path);
        path.add(reachable);
        return path;
    }

    private static boolean isConsistent(final Grid<Integer> distances) {
        final BiFunction<Cell, Cell, Boolean> edgeConsistent = (cell, neighbor) ->
            distances.get(cell) <= -1 ||
            distances.get(neighbor) <= -1 ||
            Math.abs(distances.get(cell) - distances.get(neighbor)) <= 1;
        final Predicate<Cell> cellConsistent = cell ->
            cell
                .getNeighbors(distances)
                .stream()
                .allMatch(neighbor -> edgeConsistent.apply(cell, neighbor));
        return distances.stream().map(Pair::second).allMatch(cellConsistent);
    }
}
