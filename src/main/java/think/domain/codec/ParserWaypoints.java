package think.domain.codec;

import java.util.SortedMap;
import java.util.TreeMap;
import think.domain.codec.Parser.BadMapCodeException;

final class ParserWaypoints {

    private static final int MISSING_INDEX = -1;
    private static final int START_OR_FINISH_ORDER = 1;

    private final SortedMap<Integer, Integer> orderedWaypoints; // SortedMap<Order, Index>.
    private int startIndex;
    private int finishIndex;

    ParserWaypoints() {
        this.orderedWaypoints = new TreeMap<>();
        this.startIndex = MISSING_INDEX;
        this.finishIndex = MISSING_INDEX;
    }

    void observeStart(final int index, final int order) throws BadMapCodeException {
        ParserSafety.require(order == START_OR_FINISH_ORDER);
        ParserSafety.require(startIndex == MISSING_INDEX);
        startIndex = index;
    }

    void observeFinish(final int index, final int order) throws BadMapCodeException {
        ParserSafety.require(order == START_OR_FINISH_ORDER);
        ParserSafety.require(finishIndex == MISSING_INDEX);
        finishIndex = index;
    }

    void observeWaypoint(final int index, final int order) throws BadMapCodeException {
        ParserSafety.require(order >= 1);
        ParserSafety.require(!orderedWaypoints.containsKey(order));
        orderedWaypoints.put(order, index);
    }

    int[] toOrderedArray() throws BadMapCodeException {
        ParserSafety.require(startIndex != MISSING_INDEX);
        ParserSafety.require(finishIndex != MISSING_INDEX);

        final int[] waypoints = new int[orderedWaypoints.size() + 2];
        waypoints[0] = startIndex;

        int index = 1;
        for (final int waypoint : orderedWaypoints.values()) {
            waypoints[index] = waypoint;
            index += 1;
        }
        waypoints[index] = finishIndex;
        return waypoints;
    }
}
