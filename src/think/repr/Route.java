package think.repr;

/**
    A sequence of steps to get from a source (exclusive) to a destination (inclusive),
    or empty steps if the route is blocked. Routes are board- and assignment-specific.
 */
public record Route(long source, long destination, long[] steps) {
    private static final long[] BLOCKED = new long[0];

    public Route {
        // Both zero-length direct paths and nonzero-length circular chains are illegal
        // because a point on a board is always interesting for exactly one reason.
        assert source != destination : "Cannot path to yourself.";
        if (steps.length != 0) {
            assert Grid.isNeighbor(source, steps[0]) : "First step must be neighbor.";
            assert steps[steps.length - 1] == destination : "Must end at destination.";
        }
    }

    public static Route fromChain(final Route[] routes) {
        assert routes.length > 0 : "Can't make a route of nothing.";
        for (int i = 0; i < routes.length - 1; i++) {
            assert routes[i].destination == routes[i + 1].source : "Must be connected";
        }
        final long source = routes[0].source;
        final long destination = routes[routes.length - 1].destination;
        int length = 0;
        for (final Route route : routes) {
            if (route.steps.length == 0) {
                return new Route(source, destination, BLOCKED);
            }
            length += route.steps.length;
        }
        final long[] steps = new long[length];
        int index = 0;
        for (final Route route : routes) {
            for (final long x : route.steps) {
                steps[index] = x;
                index += 1;
            }
        }
        return new Route(source, destination, steps);
    }

    public int length() {
        return steps.length;
    }

    public boolean possible() {
        return steps.length > 0;
    }
}
