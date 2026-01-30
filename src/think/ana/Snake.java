package think.ana;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import think.ana.Tools.BiOrdered;
import think.ana.Tools.Pair;
import think.repr.Cell;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Route;

/**
    Pathfinding and distance evaluation. Simulates the Pathery snake.
 */
public final class Snake {

    private Snake() {}

    public static int evaluate(final Problem problem, final HashSet<Cell> playerWalls) {
        int sum = 0;
        final Stream<Pair<Cell, Cell>> pairs = Tools.pairwise(problem.getCheckpoints());
        for (final Pair<Cell, Cell> pair : pairs.toList()) {
            final Route route = travel(
                problem,
                playerWalls,
                pair.first(),
                pair.second()
            );
            if (!route.possible()) {
                return 0;
            }
            sum += route.length();
        }
        return sum;
    }

    public static Route travel(
        final Problem problem,
        final HashSet<Cell> playerWalls,
        final Cell start,
        final Cell end
    ) {
        assert !start.equals(end);
        assert isLegalRun(problem, playerWalls, start);
        assert isLegalRun(problem, playerWalls, end);

        // We use A-star search (tree-search version) because for sparse grids with
        // connected start and end, it explores less nodes than breadth-first search.
        // If we are not seeing those grids, then use breadth-first search. The
        // Manhattan distance heuristic is fast, consistent, and admissible.
        final Function<Cell, Integer> heuristic = state -> state.manhattanTo(end);

        final HashMap<Cell, Cell> parents = new HashMap<>();
        final HashMap<Cell, Integer> gScore = new HashMap<>();
        gScore.put(start, 0);

        // LIFO tiebreaker causes DFS instead of BFS when multiple paths have equal
        // length, such as traveling diagonally with no obstacles. On a 10 by 10 grid,
        // getting from (0, 0) to (9, 9) adds 35 cells to the frontier instead of all
        // 100 with a FIFO tiebreaker.
        final PriorityQueue<BiOrdered<Cell>> frontier = new PriorityQueue<>();
        int tiebreaker = 0;
        frontier.add(new BiOrdered<>(start, heuristic.apply(start), tiebreaker--));

        // Keeping an set of visited cells to prevents balloning the frontier and is
        // simpler and faster than a custom PriorityQueue with a decreaseKey operation.
        final HashSet<Cell> internal = new HashSet<>();

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
            final BiOrdered<Cell> node = frontier.remove();
            final Cell current = node.item();
            if (!internal.add(current)) {
                continue;
            }
            if (current.equals(end)) {
                return new Route(start, end, reconstruct.apply(current));
            }
            final int gScoreCurrent = gScore.getOrDefault(current, Integer.MAX_VALUE);
            assert gScoreCurrent != Integer.MAX_VALUE;
            for (final Cell neighbor : current.getNeighbors(problem)) {
                if (!isOpen(problem, playerWalls, neighbor)) {
                    continue;
                }
                final int gScoreNew = gScoreCurrent + 1;
                final int gScoreOld = gScore.getOrDefault(neighbor, Integer.MAX_VALUE);
                if (gScoreNew >= gScoreOld || internal.contains(neighbor)) {
                    continue;
                }
                parents.put(neighbor, current);
                gScore.put(neighbor, gScoreNew);
                final int fScoreNew = gScoreNew + heuristic.apply(neighbor);
                frontier.add(new BiOrdered<>(neighbor, fScoreNew, tiebreaker--));
            }
        }
        return new Route(start, end, new ArrayList<>(0));
    }

    public static Grid<Integer> distances(
        final Problem problem,
        final HashSet<Cell> playerWalls,
        final Cell source
    ) {
        assert isLegalRun(problem, playerWalls, source);

        // -1 is sentinel unreachable value. Chosen because adding connected distance
        // grids with unreachable cells maintains the invariant that unreachable cells
        // are negative (this is untrue if the source cannot reach the destination).
        // Cells with player or system walls on them are unreachable.
        final Grid<Integer> distances = new Grid<>(
            Tools.fill(-1, problem.getRowBound() * problem.getColBound()),
            problem.getRowBound(),
            problem.getColBound()
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
                    isOpen(problem, playerWalls, neighbor) &&
                    distances.getCell(neighbor) == -1
                ) {
                    distances.setCell(neighbor, distances.getCell(current) + 1);
                    frontier.add(neighbor);
                }
            }
        }
        assert isConsistent(distances, problem);
        assert distances.getCell(source) == 0;
        return distances;
    }

    /**
        A cell is open iff it is not a system wall or player wall. A cell is empty iff
        it is open and not a checkpoint. Snakes may only step on open cells. Player
        walls may only be placed on empty cells. The set of open cells is a superset of
        empty cells.
     */
    private static boolean isOpen(
        final Problem problem,
        final HashSet<Cell> playerWalls,
        final Cell cell
    ) {
        return !problem.isSystemWall(cell) && !playerWalls.contains(cell);
    }

    private static boolean isLegalRun(
        final Problem problem,
        final HashSet<Cell> playerWalls,
        final Cell source
    ) {
        final boolean sourceIn = problem.containsCell(source);
        final boolean openSource = isOpen(problem, playerWalls, source);
        final boolean playerWallsIn = playerWalls
            .stream()
            .allMatch(problem::containsCell);
        final boolean noOverlap = playerWalls.stream().noneMatch(problem::isSystemWall);
        return sourceIn && openSource && playerWallsIn && noOverlap;
    }

    private static boolean isConsistent(
        final Grid<Integer> distances,
        final Problem problem
    ) {
        final BiFunction<Cell, Cell, Boolean> edgeConsistent = (cell, neighbor) ->
            distances.getCell(cell) <= -1 ||
            distances.getCell(neighbor) <= -1 ||
            Math.abs(distances.getCell(cell) - distances.getCell(neighbor)) <= 1;
        final Predicate<Cell> cellConsistent = cell ->
            cell
                .getNeighbors(problem)
                .stream()
                .allMatch(neighbor -> edgeConsistent.apply(cell, neighbor));
        return distances.cellStream().allMatch(cellConsistent);
    }
}
