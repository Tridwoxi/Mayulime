package think.repr;

import java.util.ArrayList;
import think.ana.Tools;
import think.ana.Tools.Counter;

/**
    Result of a Snake's travels from start (exclusive) to end (inclusive). Steps will be empty if the route is blocked. Routes are problem- and assignment-specific.
 */
public final class Route {

    private final Cell start;
    private final Cell end;
    private final Counter<Cell> steps;
    private final int length;

    public Route(final Cell start, final Cell end, final ArrayList<Cell> steps) {
        this(start, end, new Counter<>(steps));
        // Both zero-length direct paths and nonzero-length circular chains are illegal
        // because a cell on a problem is always interesting for exactly one reason.
        if (!steps.isEmpty()) {
            assert steps.getFirst().isNeighbor(start);
            assert steps.getLast().equals(end);
        }
        assert Tools.pairwise(steps).allMatch(p -> p.a().isNeighbor(p.b()));
        assert steps.size() == this.steps.totalCount();
    }

    private Route(final Cell start, final Cell end, final Counter<Cell> steps) {
        assert !start.equals(end);
        this.start = start;
        this.end = end;
        this.steps = steps;
        this.length = steps.totalCount();
    }

    public int length() {
        assert possible() : "Caller should check with Route::possible() first.";
        return length;
    }

    public boolean possible() {
        return length > 0;
    }

    public static Route fromChain(final ArrayList<Route> routes) {
        assert routes.size() > 0;
        assert Tools.pairwise(routes).allMatch(pair ->
            pair.a().end.equals(pair.b().start)
        );
        final Cell start = routes.getFirst().start;
        final Cell end = routes.getLast().end;
        final Counter<Cell> steps = new Counter<>();
        if (routes.stream().allMatch(route -> route.possible())) {
            routes.forEach(route -> steps.addAll(route.steps));
            assert routes
                .stream()
                .mapToInt(route -> route.steps.totalCount())
                .sum() ==
            steps.totalCount();
        }
        return new Route(start, end, steps);
    }
}
