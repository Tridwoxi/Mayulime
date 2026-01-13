package think.repr;

import java.util.ArrayList;
import java.util.stream.Collectors;
import think.ana.Tools;

/**
    A sequence of steps to get from a start (exclusive) to an end (inclusive),
    or empty steps if the route is blocked. Routes are problem- and assignment-specific.
 */
public record Route(Cell start, Cell end, ArrayList<Cell> steps) {
    private static final ArrayList<Cell> BLOCKED = new ArrayList<>(0);

    public Route {
        // Both zero-length direct paths and nonzero-length circular chains are illegal
        // because a cell on a problem is always interesting for exactly one reason.
        assert start != end;
        if (!steps.isEmpty()) {
            assert steps.getFirst().isNeighbor(start);
            assert steps.getLast() == end;
        }
        assert Tools.pairwiseStream(steps).allMatch(p -> p.a().isNeighbor(p.b()));
    }

    public static Route fromChain(final ArrayList<Route> routes) {
        assert routes.size() > 0;
        assert Tools.pairwiseStream(routes).allMatch(
            pair -> pair.a().end == pair.b().start
        );

        final Cell start = routes.getFirst().start;
        final Cell end = routes.getLast().end;
        if (routes.stream().anyMatch(route -> route.steps.isEmpty())) {
            return new Route(start, end, BLOCKED);
        }
        final ArrayList<ArrayList<Cell>> steps2d = routes
            .stream()
            .map(route -> route.steps)
            .collect(Collectors.toCollection(ArrayList::new));
        final ArrayList<Cell> steps = Tools.flatten(steps2d);
        return new Route(start, end, steps);
    }

    public int length() {
        return steps.size();
    }

    public boolean possible() {
        return !steps.isEmpty();
    }
}
