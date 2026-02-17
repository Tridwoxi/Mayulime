package think.ana;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import think.repr.Grid;
import think.repr.Grid.Cell;
import think.repr.Problem;
import think.repr.Problem.Feature;
import think.tools.Iteration;
import think.tools.Structures.Pair;

/**
    Pathfinding and evaluation.
 */
public final class Snake {

    private Snake() {}

    /**
        Calculate the score of the solution to the problem.

        It is a design error to pass an invalid solution to this method.
     */
    public static int evaluate(final Problem problem, final Grid<Feature> solution) {
        assert problem.isValid(solution);
        return travel(problem, solution).orElse(new ArrayList<>(0)).size();
    }

    /**
        Simulate the Pathery snake between all checkpoints with all teleports active.

        If present, the resulting list contains every cell the snake gets to by taking
        a step, in order of time. The start and teleport-outs are not automatically
        included because the snake appearing at those location does not count as having
        taken a step to get there. If a snake steps on a cell multiple times, the cell
        is included multiple times.
     */
    public static Optional<ArrayList<Cell>> travel(
        final Problem problem,
        final Grid<Feature> solution
    ) {
        final ArrayList<Cell> checkpoints = problem.getCheckpoints();
        final HashMap<Cell, Cell> teleportMap = problem.getTeleports();
        final HashSet<Cell> activeTeleports = new HashSet<>(
            problem.getTeleports().keySet()
        );
        final ArrayList<ArrayList<Cell>> paths = new ArrayList<>();
        for (final Pair<Cell, Cell> pair : Iteration.pairwise(checkpoints).toList()) {
            final Optional<ArrayList<Cell>> path = travel(
                solution,
                pair.first(),
                pair.second(),
                activeTeleports,
                teleportMap
            );
            if (path.isEmpty()) {
                return Optional.empty();
            }
            paths.add(path.orElseThrow());
        }
        return Optional.of(flatten(paths));
    }

    /**
        Simulate the Pathery snake between two checkpoints with the given teleports
        active.

        If travel is possible, activeTeleports will be modified in place to indicate
        which teleports are no longer active. If travel is impossible, the state of
        activeTeleports is unspecified. Teleportation is instant and does not add a
        step.

        It is possible for a route that was originally possible without teleports to
        become impossible when teleports are added; such happens when the snake steps
        on a teleport and gets trapped in a box.
     */
    public static Optional<ArrayList<Cell>> travel(
        final Grid<Feature> solution,
        final Cell start,
        final Cell end,
        final HashSet<Cell> activeTeleports,
        final HashMap<Cell, Cell> teleportMap
    ) {
        assert !start.equals(end);
        assert isOpen(solution.get(start)) && solution.inBounds(start);
        assert isOpen(solution.get(end)) && solution.inBounds(end);
        assert activeTeleports.stream().allMatch(teleportMap::containsKey);

        final Grid<Integer> distanceFromEnd = Distances.distanceFrom(solution, end);
        final ArrayList<ArrayList<Cell>> paths = new ArrayList<>();
        final int maxAttempts = activeTeleports.size() + 1;
        Cell currentLocation = start;

        for (int attempt = 0; attempt < maxAttempts; attempt += 1) {
            if (distanceFromEnd.get(currentLocation) <= -1) {
                return Optional.empty();
            }
            final ArrayList<Cell> path = Distances.reconstructPath(
                distanceFromEnd,
                end,
                currentLocation
            );
            final Optional<Cell> stoppedAtTeleport = path
                .stream()
                .filter(activeTeleports::contains)
                .findFirst();
            if (stoppedAtTeleport.isEmpty()) {
                paths.add(path);
                return Optional.of(flatten(paths));
            }
            final ArrayList<Cell> trimmed = trimTo(
                path,
                stoppedAtTeleport.orElseThrow()
            );
            paths.add(trimmed);
            activeTeleports.remove(stoppedAtTeleport.get());
            currentLocation = teleportMap.get(stoppedAtTeleport.get());
        }
        // This assertion is impossible to trip because the snake must consume a
        // teleport or die each step, and there are only maxAttempts teleports.
        throw new AssertionError();
    }

    /**
        A cell is open iff it does not contain a system wall or player wall. A cell is
        empty iff it is open and not a checkpoint or teleport. Snakes may only step on
        open cells. Player walls may only be placed on empty cells. The set of open
        cells is an improper superset of the set of empty cells.
     */
    static boolean isOpen(final Feature feature) {
        return switch (feature) {
            case EMPTY -> true;
            case CHECKPOINT -> true;
            case SYSTEM_WALL -> false;
            case PLAYER_WALL -> false;
            case TELEPORT_IN -> true;
            case TELEPORT_OUT -> true;
        };
    }

    private static ArrayList<Cell> trimTo(final ArrayList<Cell> path, final Cell end) {
        assert path.contains(end);
        final ArrayList<Cell> trimmed = new ArrayList<>(path.size());
        for (final Cell step : path) {
            trimmed.add(step);
            if (step.equals(end)) {
                break;
            }
        }
        return trimmed;
    }

    private static ArrayList<Cell> flatten(final ArrayList<ArrayList<Cell>> paths) {
        return Iteration.materialize(paths.stream().flatMap(ArrayList::stream));
    }
}
