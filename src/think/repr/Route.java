package think.repr;

import java.util.ArrayList;
import think.ana.Tools;

/**
    A sequence of steps to get from a source (exclusive) to a destination (inclusive),
    or empty steps if the route is blocked. Routes are board- and assignment-specific.
 */
public record Route(
    Problem problem,
    Assignment assignment,
    Point source,
    Point destination,
    ArrayList<Point> steps
) {
    public Route {
        // Both zero-length direct paths and nonzero-length circular chains are illegal
        // because a point on a board is always interesting for exactly one reason.
        assert !source.equals(destination) : "Cannot path to yourself.";
        if (!steps.isEmpty()) {
            assert steps.getFirst().isNeighbor(source) : "First step must be neighbor.";
            assert steps.getLast().equals(destination) : "Must end at destination.";
        }
    }

    public static Route fromChain(ArrayList<Route> routes) {
        assert !routes.isEmpty() : "Can't make a route of nothing.";
        assert Tools.pairwiseStream(routes).allMatch(p ->
            p.a().destination.equals(p.b().source)
        ) : "Routes must be connected.";
        assert Tools.pairwiseStream(routes).allMatch(
            p ->
                p.a().problem.equals(p.b().problem) &&
                p.a().assignment.equals(p.b().assignment)
        ) : "Routes must have the same assignment and problem.";

        ArrayList<Point> steps = new ArrayList<>();
        if (routes.stream().allMatch(p -> p.possible())) {
            routes.forEach(r -> steps.addAll(r.steps));
        }
        return new Route(
            routes.getFirst().problem,
            routes.getFirst().assignment,
            routes.getFirst().source,
            routes.getLast().destination,
            new ArrayList<>(0)
        );
    }

    public int length() {
        return steps.size();
    }

    public boolean possible() {
        return steps.size() > 0;
    }
}
