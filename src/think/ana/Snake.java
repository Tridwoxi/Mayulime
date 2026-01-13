package think.ana;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import think.ana.Tools.AStarQueue;
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
        assert !start.equals(end);
        assert legalRun(problem, rubbers, start);
        assert legalRun(problem, rubbers, end);

        // We use A-star search (tree-search version) because for sparse grids with
        // connected start and end, it explores less nodes than breadth-first search.
        // If we are not seeing those grids, then use breadth-first search.
        final Function<Cell, Integer> heuristic = c -> c.manhattan(end);

        final HashMap<Cell, Cell> parents = new HashMap<>();
        final HashMap<Cell, Integer> gScore = new HashMap<>();
        gScore.put(start, 0);
        final AStarQueue<Cell> frontier = new AStarQueue<>();
        frontier.add(start, heuristic.apply(start));

        final Function<Cell, ArrayList<Cell>> reconstruct = current -> {
            final ArrayList<Cell> path = new ArrayList<>();
            while (parents.containsKey(current)) {
                path.add(current);
                current = parents.get(current);
            }
            Collections.reverse(path);
            assert !path.contains(start) && path.contains(end);
            return path;
        };

        while (!frontier.isEmpty()) {
            final Cell current = frontier.remove();
            if (current.equals(end)) {
                return new Route(start, end, reconstruct.apply(current));
            }
            final int gScoreCurrent = gScore.getOrDefault(current, Integer.MAX_VALUE);
            assert gScoreCurrent != Integer.MAX_VALUE;
            for (final Cell neighbor : current.getNeighbors(problem)) {
                if (!isOpen(problem, rubbers, neighbor)) {
                    continue;
                }
                final int gScoreNew = gScoreCurrent + 1;
                final int gScoreOld = gScore.getOrDefault(neighbor, Integer.MAX_VALUE);
                if (gScoreNew >= gScoreOld) {
                    continue;
                }
                parents.put(neighbor, current);
                gScore.put(neighbor, gScoreNew);
                final int fScoreNew = gScoreNew + heuristic.apply(neighbor);
                if (frontier.contains(neighbor)) {
                    frontier.decrease(neighbor, fScoreNew);
                } else {
                    frontier.add(neighbor, fScoreNew);
                }
            }
        }
        return new Route(start, end, new ArrayList<>(0));
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
