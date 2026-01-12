package think.ana;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import think.repr.Grid;
import think.repr.Point;
import think.repr.Problem;
import think.repr.Route;

public final class Snake {

    public Route travel(
        final Problem problem,
        final HashSet<Point> rubbers,
        final Point source,
        final Point destination
    ) {
        // Standard breadth-first search. TODO: delete, replace with astar.
        assert source != destination;
        assert !problem.isBrick(source);
        assert !problem.isBrick(destination);
        assert !rubbers.contains(source);
        assert !rubbers.contains(destination);
        assert rubbers
            .stream()
            .allMatch(
                p ->
                    p.i() >= 0 &&
                    p.i() < problem.getBoundI() &&
                    p.j() >= 0 &&
                    p.j() < problem.getBoundJ()
            );
        assert rubbers.stream().noneMatch(problem::isBrick);

        final HashSet<Point> visited = new HashSet<>();
        final HashMap<Point, Point> parents = new HashMap<>();
        final ArrayDeque<Point> frontier = new ArrayDeque<>();
        visited.add(source);
        frontier.add(source);
        boolean reached = false;
        while (!frontier.isEmpty() && !reached) {
            final Point current = frontier.removeFirst();
            for (final Point neighbor : current.getNeighbors(problem)) {
                if (
                    visited.contains(neighbor) ||
                    problem.isBrick(neighbor) ||
                    rubbers.contains(neighbor)
                ) {
                    continue;
                }
                visited.add(neighbor);
                parents.put(neighbor, current);
                if (neighbor.equals(destination)) {
                    reached = true;
                    break;
                }
                frontier.add(neighbor);
            }
        }

        if (!reached) {
            return new Route(source, destination, new ArrayList<>(0));
        }
        final ArrayList<Point> steps = new ArrayList<>();
        Point current = destination;
        while (!current.equals(source)) {
            steps.add(current);
            final Point next = parents.get(current);
            assert next != null;
            current = next;
        }
        Collections.reverse(steps);
        return new Route(source, destination, steps);
    }

    public Grid<Integer> distances(
        final Problem problem,
        final HashSet<Point> rubbers,
        final Point source
    ) {
        assert isOpen(problem, rubbers, source);

        // -1 is sentinel unreachable value. Chosen because adding connected distance
        // grids with unreachable cells maintains the invariant that unreachable cells
        // are negative (this is untrue if the source cannot reach the destination).
        // Cells with rubbers or bricks on them are unreachable.
        final Grid<Integer> distances = new Grid<>(
            Tools.fill(-1, problem.getAllPoints().size()),
            problem.getBoundI(),
            problem.getBoundJ()
        );

        // We use breadth-first search because we visit every reachable point and it
        // has slightly less overhead than A-star. "distances" also visited set.
        final ArrayDeque<Point> frontier = new ArrayDeque<>();
        frontier.add(source);
        distances.setCell(source, 0);
        while (!frontier.isEmpty()) {
            final Point current = frontier.removeFirst();
            assert distances.getCell(current) >= 0;
            for (final Point neighbor : current.getNeighbors(problem)) {
                if (
                    isOpen(problem, rubbers, neighbor) &&
                    distances.getCell(neighbor) == -1
                ) {
                    distances.setCell(neighbor, distances.getCell(current) + 1);
                    frontier.add(neighbor);
                }
            }
        }
        assert distances.getCell(source) == 0;
        return distances;
    }

    private static boolean isOpen(
        final Problem problem,
        final HashSet<Point> rubbers,
        final Point point
    ) {
        return !problem.isBrick(point) && !rubbers.contains(point);
    }
}
