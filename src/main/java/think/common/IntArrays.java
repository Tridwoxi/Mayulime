package think.common;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntPredicate;

public final class IntArrays {

    private IntArrays() {}

    public static int[] ofRange(final int startInclusive, final int endExclusive) {
        if (startInclusive >= endExclusive) {
            throw new IllegalArgumentException();
        }
        final int[] range = new int[endExclusive - startInclusive];
        for (int index = 0; index < range.length; index += 1) {
            range[index] = startInclusive + index;
        }
        return range;
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

    public static int[] trimmedCopy(final int[] array, final int size) {
        if (size < 0 || size > array.length) {
            throw new IllegalArgumentException();
        }
        final int[] trimmed = new int[size];
        System.arraycopy(array, 0, trimmed, 0, size);
        return trimmed;
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
        return trimmedCopy(filtered, count);
    }
}
