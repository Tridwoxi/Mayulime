package think.repr;

import java.util.ArrayList;
import java.util.stream.Stream;
import think.tools.Iteration;

/**
    Result of a Snake's travels from start (exclusive) to end (inclusive). Steps will be empty if the route is blocked. Routes are problem- and assignment-specific.
 */
public final class Route {

    private final Cell start;
    private final Cell end;
    private final ArrayList<Cell> steps;

    public Route(final Cell start, final Cell end, final ArrayList<Cell> steps) {
        this.start = start;
        this.end = end;
        this.steps = new ArrayList<>(steps);
        // Both zero-length direct paths and nonzero-length circular chains are illegal
        // because a cell on a problem is always interesting for exactly one reason.
        if (!steps.isEmpty()) {
            assert steps.getFirst().isNeighbor(start);
            assert steps.getLast().equals(end);
        }
        assert Iteration.pairwise(steps).allMatch(pair ->
            pair.first().isNeighbor(pair.second())
        );
    }

    public int getLength() {
        assert isPossible() : "Caller should check with Route::isPossible() first.";
        return steps.size();
    }

    public boolean isPossible() {
        return steps.size() > 0;
    }

    public Cell getStart() {
        return start;
    }

    public Cell getEnd() {
        return end;
    }

    public Stream<Cell> walk() {
        return steps.stream();
    }

    public static int cumulativeLength(final ArrayList<Route> routes) {
        if (routes.stream().allMatch(route -> route.isPossible())) {
            return routes.stream().mapToInt(Route::getLength).sum();
        }
        return 0;
    }

    public static Stream<Cell> cumulativeWalk(final ArrayList<Route> routes) {
        if (routes.stream().allMatch(route -> route.isPossible())) {
            return routes.stream().flatMap(Route::walk);
        }
        return Stream.empty();
    }
}
