package think.repr;

import java.util.ArrayList;
import think.ana.Tools;

/**
    A sequence of steps to get from a source (exclusive) to a destination (inclusive),
    or empty steps if the route is blocked. Routes are board- and assignment-specific.
 */
public record Route(Point source, Point destination, ArrayList<Point> steps) {
    private static final ArrayList<Point> BLOCKED = new ArrayList<>();

    public Route {
        // Both zero-length direct paths and nonzero-length circular chains are illegal
        // because a point on a board is always interesting for exactly one reason.
        assert source != destination : "Cannot path to yourself.";
        if (!steps.isEmpty()) {
            assert steps.getFirst().isNeighbor(source) : "First step must be neighbor.";
            assert steps.getLast() == destination : "Must end at destination.";
        }
    }

    public static Route fromChain(final ArrayList<Route> routes) {
        assert routes.size() > 0 : "Can't make a route of nothing.";
        assert Tools.pairwiseStream(routes).allMatch(
            pair -> pair.a().destination == pair.b().source
        ) : "Must be connected";
        final Point source = routes.getFirst().source;
        final Point destination = routes.getLast().destination;
        if (routes.stream().anyMatch(route -> route.steps.isEmpty())) {
            return new Route(source, destination, BLOCKED);
        }
        final int length = routes
            .stream()
            .mapToInt(route -> route.steps.size())
            .sum();
        final ArrayList<Point> steps = new ArrayList<>(length);
        routes.forEach(route -> steps.addAll(route.steps));
        assert steps.size() == length : "Route chain steps miscounted.";
        return new Route(source, destination, steps);
    }

    public int length() {
        return steps.size();
    }

    public boolean possible() {
        return !steps.isEmpty();
    }
}
