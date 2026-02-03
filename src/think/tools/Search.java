package think.tools;

import java.util.ArrayList;
import java.util.function.Predicate;

/**
    Search-related utilities.
 */
public final class Search {

    private Search() {}

    /**
        Return the last index in trueThenFalse where the predicate evaluates to true,
        or -1 if no such index exists. It is unspecified behavior if trueThenFalse is
        not a sequence of (possibly 0) items that evaluate to true followed by a
        sequence of (possibly 0) items that evaluate to false.
     */
    public static <T> int monotonicPredicateDescending(
        final ArrayList<T> trueThenFalse,
        final Predicate<T> predicate
    ) {
        int lowestIndex = 0;
        int highestIndex = trueThenFalse.size() - 1;
        int lastTrueIndex = -1;
        while (lowestIndex <= highestIndex) {
            final int midIndex = (lowestIndex + highestIndex) / 2;
            if (predicate.test(trueThenFalse.get(midIndex))) {
                lastTrueIndex = midIndex;
                lowestIndex = midIndex + 1;
            } else {
                highestIndex = midIndex - 1;
            }
        }
        assert checkMPD(trueThenFalse, predicate, lastTrueIndex);
        return lastTrueIndex;
    }

    private static <T> boolean checkMPD(
        final ArrayList<T> trueThenFalse,
        final Predicate<T> predicate,
        final int answer
    ) {
        final int groundTruth =
            (int) Iteration.enumerate(trueThenFalse)
                .filter(ordered -> predicate.test(ordered.item()))
                .count() -
            1; // 1-based counting, 0-based indexing.
        final boolean asPromised =
            groundTruth == -1 ||
            trueThenFalse.stream().limit(groundTruth + 1).allMatch(predicate);
        return answer == groundTruth && asPromised;
    }
}
