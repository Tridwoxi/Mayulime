package think.ana;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Function;
import think.repr.Cell;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;
import think.repr.Route;
import think.tools.Iteration;
import think.tools.Iteration.Pair;
import think.tools.Ordering.BiOrdered;

/**
    Simulate the Pathery snake's pathfinding.

    This class is the leader of its package, so owns all package-private utilities.

    Although we are sometimes unable to check, and an exception might not be thrown, it
    is always a design error to pass invalid solutions to any method in this class.
 */
public final class Pathfind {

    private Pathfind() {}

    /**
        Calculate the score of the solution to the problem.
     */
    public static int evaluate(final Problem problem, final Grid<Feature> solution) {
        assert problem.isValid(solution);
        final ArrayList<Cell> checkpoints = problem.getCheckpoints();
        final HashMap<Cell, Cell> teleportMap = problem.getTeleports();
        final HashSet<Cell> activeTeleports = new HashSet<>(
            problem.getTeleports().keySet()
        );
        int totalSteps = 0;
        for (final Pair<Cell, Cell> pair : Iteration.pairwise(checkpoints).toList()) {
            final ArrayList<Route> routes = travel(
                solution,
                pair.first(),
                pair.second(),
                activeTeleports,
                teleportMap
            );
            if (routes.isEmpty()) {
                return 0;
            }
            totalSteps += routes.stream().mapToInt(Route::getLength).sum();
        }
        return totalSteps;
    }

    /**
        Get from start to end, including teleports.

        If the travel is possible, the length of the resulting list will be (the number
        of teleports the snake stepped on) + 1, and activeTeleports will be modified in
        place. If travel is impossible, the resulting list will be empty, and the state
        of activeTeleports is unspecified.

        It is possible for a route that was originally possible without teleports to
        become impossible when teleports are added. This happens when the snake steps
        on a teleport and gets trapped in a box.
     */
    public static ArrayList<Route> travel(
        final Grid<Feature> solution,
        final Cell start,
        final Cell end,
        final HashSet<Cell> activeTeleports,
        final HashMap<Cell, Cell> teleportMap
    ) {
        assert !start.equals(end);
        assert isLegalRun(solution, start) && isLegalRun(solution, end);
        assert activeTeleports.stream().allMatch(teleportMap::containsKey);

        final ArrayList<Route> routes = new ArrayList<>();
        Cell currentLocation = start;
        final int maxAttempts = activeTeleports.size() + 1;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            final Route route = travel(solution, currentLocation, end);
            if (!route.isPossible()) {
                return new ArrayList<>(0);
            }
            final Optional<Cell> stoppedAtTeleport = route
                .walk()
                .filter(activeTeleports::contains)
                .findFirst();
            if (stoppedAtTeleport.isEmpty()) {
                routes.add(route);
                return routes;
            }
            assert activeTeleports.contains(stoppedAtTeleport.get());
            final Route trimmed = Route.trimTo(route, stoppedAtTeleport.get());
            routes.add(trimmed);
            activeTeleports.remove(trimmed.getEnd());
            // Teleportation is instant and does not add a step.
            currentLocation = teleportMap.get(trimmed.getEnd());
        }
        throw new AssertionError();
    }

    /**
        Get from start to end, ignoring teleports.
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public static Route travel(
        final Grid<Feature> solution,
        final Cell start,
        final Cell end
    ) {
        assert !start.equals(end);
        assert isLegalRun(solution, start) && isLegalRun(solution, end);

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
            // After insertion into the PriorityQueue, this algorithm explores nodes in
            // reverse insertion order, so inserting LDRU causes URDL exploration,
            // which is the correct snake preference order.
            for (final Cell neighbor : current.getNeighborsOnLDRU(solution)) {
                if (!isOpen(solution, neighbor)) {
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

    // == Package-private API. =========================================================

    /**
        A cell is open iff it does not contain a system wall or player wall. A cell is
        empty iff it is open and not a checkpoint or teleport. Snakes may only step on
        open cells. Player walls may only be placed on empty cells. The set of open
        cells is an improper superset of the set of empty cells.
     */
    static boolean isOpen(final Grid<Feature> solution, final Cell cell) {
        return switch (solution.get(cell)) {
            case EMPTY -> true;
            case CHECKPOINT -> true;
            case SYSTEM_WALL -> false;
            case PLAYER_WALL -> false;
            case TELEPORT_IN -> true;
            case TELEPORT_OUT -> true;
        };
    }

    static boolean isLegalRun(final Grid<Feature> solution, final Cell source) {
        return solution.inBounds(source) && isOpen(solution, source);
    }
}
