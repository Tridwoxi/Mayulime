package think.ana;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;
import think.repr.Cell;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.Feature;
import think.tools.Iteration;
import think.tools.Structures.Pair;

/**
    Pathfinding and evaluation.

    Although we are sometimes unable to check, and an exception might not be thrown, it
    is always a design error to pass invalid solutions to any method in this class.
 */
public final class Pathfind {

    /**
        Result of a Snake's travels. Excludes start. Includes end. Cells may be
        disconnected due to teleporation.
     */
    public static final class Path extends ArrayList<Cell> {

        private Path(final int initialCapacity) {
            super(initialCapacity);
        }

        private Path(final Collection<Cell> cells) {
            super(cells);
        }
    }

    private Pathfind() {}

    /**
        Calculate the score of the solution to the problem.
     */
    public static int evaluate(final Problem problem, final Grid<Feature> solution) {
        assert problem.isValid(solution);
        return travel(problem, solution).orElse(new Path(0)).size();
    }

    /**
        Simulate the Pathery snake between all checkpoints.
     */
    public static Optional<Path> travel(
        final Problem problem,
        final Grid<Feature> solution
    ) {
        final ArrayList<Cell> checkpoints = problem.getCheckpoints();
        final HashMap<Cell, Cell> teleportMap = problem.getTeleports();
        final HashSet<Cell> activeTeleports = new HashSet<>(
            problem.getTeleports().keySet()
        );
        final ArrayList<Path> paths = new ArrayList<>();
        for (final Pair<Cell, Cell> pair : Iteration.pairwise(checkpoints).toList()) {
            final Optional<Path> path = travel(
                solution,
                pair.first(),
                pair.second(),
                activeTeleports,
                teleportMap
            );
            if (path.isEmpty()) {
                return Optional.empty();
            }
            paths.add(path.get());
        }
        return Optional.of(flatten(paths));
    }

    /**
        Simulate the Pathery snake between two checkpoints.

        If travel is possible, activeTeleports will be modified in place to indicate
        which teleports are no longer active. If the travel is the state of
        activeTeleports is unspecified.

        It is possible for a route that was originally possible without teleports to
        become impossible when teleports are added. This happens when the snake steps
        on a teleport and gets trapped in a box.
     */
    public static Optional<Path> travel(
        final Grid<Feature> solution,
        final Cell start,
        final Cell end,
        final HashSet<Cell> activeTeleports,
        final HashMap<Cell, Cell> teleportMap
    ) {
        assert !start.equals(end);
        assert isLegalRun(solution, start) && isLegalRun(solution, end);
        assert activeTeleports.stream().allMatch(teleportMap::containsKey);

        final ArrayList<Path> paths = new ArrayList<>();
        Cell currentLocation = start;
        final int maxAttempts = activeTeleports.size() + 1;
        for (int attempt = 0; attempt < maxAttempts; attempt += 1) {
            final Optional<Path> path = travel(solution, currentLocation, end);
            if (path.isEmpty()) {
                return Optional.empty();
            }
            final Optional<Cell> stoppedAtTeleport = path
                .get()
                .stream()
                .filter(activeTeleports::contains)
                .findFirst();
            if (stoppedAtTeleport.isEmpty()) {
                paths.add(path.get());
                return Optional.of(flatten(paths));
            }
            assert activeTeleports.contains(stoppedAtTeleport.get());
            final Path trimmed = trimTo(path.get(), stoppedAtTeleport.get());
            paths.add(trimmed);
            activeTeleports.remove(stoppedAtTeleport.get());
            // Teleportation is instant and does not add a step.
            currentLocation = teleportMap.get(stoppedAtTeleport.get());
        }
        // This assertion is impossible to trip because the snake must consume a
        // teleport or die each step, and there are only maxAttempts teleports.
        throw new AssertionError();
    }

    /**
        Simulate an alternative version of the Pathery snake that is not affected by
        teleports.
     */
    public static Optional<Path> travel(
        final Grid<Feature> solution,
        final Cell start,
        final Cell end
    ) {
        assert isLegalRun(solution, start) && isLegalRun(solution, end);

        // We use breadth-first search. From the task specification: "Among shortest
        // paths, the Snake prefers to go up, then right, then down, then left.".
        // Asymtopically faster algorithms like A-star find the wrong shortest path, have
        // have higher constant factors, and perform badly on grids dense grids with
        // disconnected start and finish.

        // PERF: Unbox primitives, use raw indexes instead of Cells, unroll and inline
        // neighbor loop, 1d primitive arrays, preallocate and reuse where possible.

        final int numRows = solution.getNumRows();
        final int numCols = solution.getNumCols();
        final Grid<Boolean> visited = new Grid<>(false, numRows, numCols);
        final Grid<Cell> parents = new Grid<>(Cell.OUT_OF_BOUNDS, numRows, numCols);
        final ArrayDeque<Cell> frontier = new ArrayDeque<>();

        final Function<Cell, Path> reverse = cell -> {
            final Path steps = new Path(10);
            Cell walker = end;
            while (!walker.equals(start)) {
                steps.add(walker);
                walker = parents.get(walker);
            }
            Collections.reverse(steps);
            assert !steps.contains(Cell.OUT_OF_BOUNDS);
            return steps;
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
                    return Optional.of(reverse.apply(neighbor));
                }
                frontier.add(neighbor);
            }
        }
        return Optional.empty();
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

    // == Private API. =================================================================

    private static Path flatten(final ArrayList<Path> paths) {
        return new Path(paths.stream().flatMap(Path::stream).toList());
    }

    private static Path trimTo(final Path path, final Cell end) {
        assert path.contains(end);
        final Path trimmed = new Path(path.size());
        for (final Cell step : path) {
            trimmed.add(step);
            if (step.equals(end)) {
                break;
            }
        }
        return trimmed;
    }
}
