package think.ana;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.function.BiFunction;
import think.repr.Cell;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Route;

/**
    Distance evaluator. Simulates the Pathery snake.
 */
public final class Snake {

    public Route travel(
        final Problem problem,
        final HashSet<Cell> rubbers,
        final Cell start,
        final Cell end
    ) {
        // Breadth-first search finds the shortest path on an unweighted grid.
        assert start != end;
        assert legalRun(problem, rubbers, start);
        assert legalRun(problem, rubbers, end);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Grid<Integer> distances(
        final Problem problem,
        final HashSet<Cell> rubbers,
        final Cell source
    ) {
        assert legalRun(problem, rubbers, source);
        // -1 is sentinel unreachable value. Chosen because adding connected distance
        // grids with unreachable cells maintains the invariant that unreachable cells
        // are negative (this is untrue if the source cannot reach the destination).
        // Cells with rubbers or bricks on them are unreachable.
        final Grid<Integer> distances = new Grid<>(
            Tools.fill(-1, problem.getAllCells().size()),
            problem.getBoundI(),
            problem.getBoundJ()
        );
        // We use breadth-first search because we visit every reachable point and it
        // has slightly less overhead than A-star. "distances" also visited set.
        final ArrayDeque<Cell> frontier = new ArrayDeque<>();
        frontier.add(source);
        distances.setCell(source, 0);
        while (!frontier.isEmpty()) {
            final Cell current = frontier.removeFirst();
            assert distances.getCell(current) >= 0;
            for (final Cell neighbor : current.getNeighbors(problem)) {
                if (
                    isOpen(problem, rubbers, neighbor) &&
                    distances.getCell(neighbor) == -1
                ) {
                    distances.setCell(neighbor, distances.getCell(current) + 1);
                    frontier.add(neighbor);
                }
            }
        }
        final BiFunction<Cell, Cell, Boolean> consistent = (p, n) ->
            distances.getCell(p) == -1 ||
            distances.getCell(n) == -1 ||
            Math.abs(distances.getCell(p) - distances.getCell(n)) <= 1;
        assert distances
            .cellStream()
            .allMatch(p ->
                p.getNeighbors(problem).stream().allMatch((n -> consistent.apply(p, n)))
            );
        assert distances.getCell(source) == 0;
        return distances;
    }

    /**
        A cell is open iff it is not a brick or rubber. A cell is empty iff it is open
        and not a checkpoint. Snakes may only step on open cells. Rubbers may only be
        placed on empty cells. The set of open cells is a superset of empty cells.
     */
    private static boolean isOpen(
        final Problem problem,
        final HashSet<Cell> rubbers,
        final Cell cell
    ) {
        return !problem.isBrick(cell) && !rubbers.contains(cell);
    }

    private static boolean legalRun(
        final Problem problem,
        final HashSet<Cell> rubbers,
        final Cell source
    ) {
        final boolean sourceIn = problem.containsCell(source);
        final boolean openSource = isOpen(problem, rubbers, source);
        final boolean rubbersIn = rubbers.stream().allMatch(problem::containsCell);
        final boolean noOverlap = rubbers.stream().noneMatch(problem::isBrick);
        return sourceIn && openSource && rubbersIn && noOverlap;
    }
}
