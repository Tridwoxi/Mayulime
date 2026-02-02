package think.ana;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;
import think.repr.Cell;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;
import think.repr.Route;
import think.tools.Iteration;
import think.tools.Structures.Pair;

/**
    Simulate the Pathery snake's pathfinding.

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
    public static Route travel(
        final Grid<Feature> solution,
        final Cell start,
        final Cell end
    ) {
        assert isLegalRun(solution, start) && isLegalRun(solution, end);

        // We use breadth-first search. From the task specification: "Among shortest
        // paths, the Snake prefers to go up, then right, then down, then left.".
        // Faster algorithms like A-star may find a shortest path in less time, but the
        // returned path may not match the Snake's preference, and patching the result
        // to match the Snake's preference may not be worth the effort.

        // Even if we were able to use A-star, it may not be faster. A-star wins on
        // large sparse grids with connected start and finish. Our grids are small
        // (perhaps 1k cells), and freqently dense and disconnected.

        final int numRows = solution.getNumRows();
        final int numCols = solution.getNumCols();
        final Grid<Boolean> visited = new Grid<>(false, numRows, numCols);
        final Grid<Cell> parents = new Grid<>(Cell.OUT_OF_BOUNDS, numRows, numCols);
        final ArrayDeque<Cell> frontier = new ArrayDeque<>();

        final Function<Cell, Route> reverse = cell -> {
            final ArrayList<Cell> steps = new ArrayList<>();
            Cell walker = end;
            while (!walker.equals(start)) {
                steps.add(walker);
                walker = parents.get(walker);
            }
            Collections.reverse(steps);
            assert !steps.contains(Cell.OUT_OF_BOUNDS);
            return new Route(start, end, steps);
        };

        visited.set(start, true);
        frontier.add(start);
        while (!frontier.isEmpty()) {
            final Cell current = frontier.removeFirst();
            // Cells are taken out of the frontier in the same order they are inserted.
            // Hence URDL is the correct order.
            for (final Cell neighbor : current.getNeighborsURDL(solution)) {
                if (visited.get(neighbor) || !isOpen(solution, neighbor)) {
                    continue;
                }
                visited.set(neighbor, true);
                parents.set(neighbor, current);
                if (neighbor.equals(end)) {
                    return reverse.apply(neighbor);
                }
                frontier.add(neighbor);
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
