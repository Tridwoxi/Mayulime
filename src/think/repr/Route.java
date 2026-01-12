package think.repr;

import java.util.ArrayList;

/**
    A sequence of steps to get from a source (exclusive) to a destination (inclusive),
    or empty steps if the route is blocked. Routes are board- and assignment-specific.
 */
public record Route(long source, long destination, ArrayList<Long> steps) {
    private static final ArrayList<Long> BLOCKED = new ArrayList<>();

    public Route {
        assert steps != null : "Steps must be provided.";
        steps = new ArrayList<>(steps);
        // Both zero-length direct paths and nonzero-length circular chains are illegal
        // because a point on a board is always interesting for exactly one reason.
        assert source != destination : "Cannot path to yourself.";
        if (!steps.isEmpty()) {
            assert Grid.isNeighbor(
                source,
                steps.get(0)
            ) : "First step must be neighbor.";
            assert steps.get(steps.size() - 1) ==
            destination : "Must end at destination.";
        }
    }

    public static Route fromChain(final ArrayList<Route> routes) {
        assert routes.size() > 0 : "Can't make a route of nothing.";
        for (int i = 0; i < routes.size() - 1; i++) {
            assert routes.get(i).destination ==
            routes.get(i + 1).source : "Must be connected";
        }
        final long source = routes.get(0).source;
        final long destination = routes.get(routes.size() - 1).destination;
        int length = 0;
        for (final Route route : routes) {
            if (route.steps.isEmpty()) {
                return new Route(source, destination, BLOCKED);
            }
            length += route.steps.size();
        }
        final ArrayList<Long> steps = new ArrayList<>(length);
        for (final Route route : routes) {
            steps.addAll(route.steps);
        }
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
