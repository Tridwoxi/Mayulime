package think.domain.codec;

import java.util.SortedMap;
import java.util.TreeMap;
import think.domain.codec.Parser.BadMapCodeException;

final class ParserCheckpoints {

    private static final int MISSING_INDEX = -1;
    private static final int START_OR_FINISH_ORDER = 1;

    private final SortedMap<Integer, Integer> orderedCheckpoints; // SortedMap<Order, Index>.
    private int startIndex;
    private int finishIndex;

    ParserCheckpoints() {
        this.orderedCheckpoints = new TreeMap<>();
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

    void observeCheckpoint(final int index, final int order) throws BadMapCodeException {
        ParserSafety.require(order >= 1);
        ParserSafety.require(!orderedCheckpoints.containsKey(order));
        orderedCheckpoints.put(order, index);
    }

    int[] toOrderedArray() throws BadMapCodeException {
        ParserSafety.require(startIndex != MISSING_INDEX);
        ParserSafety.require(finishIndex != MISSING_INDEX);

        final int[] checkpoints = new int[orderedCheckpoints.size() + 2];
        checkpoints[0] = startIndex;

        int index = 1;
        for (final int checkpoint : orderedCheckpoints.values()) {
            checkpoints[index] = checkpoint;
            index += 1;
        }
        checkpoints[index] = finishIndex;
        return checkpoints;
    }
}
