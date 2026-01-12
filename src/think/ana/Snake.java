package think.ana;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
        // Standard breadth-first search.
        assert source != destination;
        assert !problem.isBrick(source);
        assert !problem.isBrick(destination);
        assert !rubbers.contains(source);
        assert !rubbers.contains(destination);

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
            assert next != null : "Reached node must have a parent.";
            current = next;
        }
        Collections.reverse(steps);
        return new Route(source, destination, steps);
    }
}
