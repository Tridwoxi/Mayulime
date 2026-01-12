package think.repr;

import java.util.ArrayList;
import java.util.stream.Collectors;
import think.ana.Tools;

/**
    A sequence of steps to get from a source (exclusive) to a destination (inclusive),
    or empty steps if the route is blocked. Routes are board- and assignment-specific.
 */
public record Route(Point source, Point destination, ArrayList<Point> steps) {
    private static final ArrayList<Point> BLOCKED = new ArrayList<>(0);

    public Route {
        // Both zero-length direct paths and nonzero-length circular chains are illegal
        // because a point on a board is always interesting for exactly one reason.
        assert source != destination;
        if (!steps.isEmpty()) {
            assert steps.getFirst().isNeighbor(source);
            assert steps.getLast() == destination;
        }
        assert Tools.pairwiseStream(steps).allMatch(p -> p.a().isNeighbor(p.b()));
    }

    public static Route fromChain(final ArrayList<Route> routes) {
        assert routes.size() > 0;
        assert Tools.pairwiseStream(routes).allMatch(
            pair -> pair.a().destination == pair.b().source
        );

        final Point source = routes.getFirst().source;
        final Point destination = routes.getLast().destination;
        if (routes.stream().anyMatch(route -> route.steps.isEmpty())) {
            return new Route(source, destination, BLOCKED);
        }
        final ArrayList<ArrayList<Point>> steps2d = routes
            .stream()
            .map(route -> route.steps)
            .collect(Collectors.toCollection(ArrayList::new));
        final ArrayList<Point> steps = Tools.flatten(steps2d);
        return new Route(source, destination, steps);
    }

    public int length() {
        return steps.size();
    }

    public boolean possible() {
        return !steps.isEmpty();
    }
}
