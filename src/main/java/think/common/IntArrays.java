package think.common;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntPredicate;

public final class IntArrays {

    private IntArrays() {}

    public static int[] ofConstant(final int value, final int length) {
        final int[] array = new int[length];
        Arrays.fill(array, value);
        return array;
    }

    public static int[] ofRange(final int startInclusive, final int endExclusive) {
        if (startInclusive > endExclusive) {
            throw new IllegalArgumentException();
        }
        final int[] range = new int[endExclusive - startInclusive];
        for (int index = 0; index < range.length; index += 1) {
            range[index] = startInclusive + index;
        }
        return range;
    }

    public static int[] ofRangeWhere(
        final int startInclusive,
        final int endExclusive,
        final IntPredicate keepIff
    ) {
        if (startInclusive > endExclusive) {
            throw new IllegalArgumentException();
        }
        final int[] filtered = new int[endExclusive - startInclusive];
        int count = 0;
        for (int index = 0; index < filtered.length; index += 1) {
            final int value = startInclusive + index;
            if (keepIff.test(value)) {
                filtered[count] = value;
                count += 1;
            }
        }
        return slicedCopy(filtered, 0, count);
    }

    public static void shuffleInPlace(final int[] array) {
        // Reference: https://en.wikipedia.org/wiki/Fisher–Yates_shuffle
        for (int i = array.length - 1; i > 0; i--) {
            final int j = ThreadLocalRandom.current().nextInt(i + 1);
            final int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    public static int[] slicedCopy(
        final int[] array,
        final int startInclusive,
        final int endExclusive
    ) {
        if (startInclusive < 0 || endExclusive > array.length || startInclusive > endExclusive) {
            throw new IllegalArgumentException();
        }
        final int[] sliced = new int[endExclusive - startInclusive];
        System.arraycopy(array, startInclusive, sliced, 0, sliced.length);
        return sliced;
    }

    public static int[] filteredCopy(final int[] array, final IntPredicate keepIff) {
        final int[] filtered = new int[array.length];
        int count = 0;
        for (final int value : array) {
            if (keepIff.test(value)) {
                filtered[count] = value;
                count += 1;
            }
        }
        return slicedCopy(filtered, 0, count);
    }
}
