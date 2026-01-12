package think.ana;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
    Small miscellany.
 */
public final class Tools {

    // == List manipulation. ===========================================================

    public static <T> ArrayList<T> flatten(ArrayList<ArrayList<T>> lists) {
        final int length = lists
            .stream()
            .mapToInt(x -> x.size())
            .sum();
        final ArrayList<T> result = new ArrayList<>(length);
        for (ArrayList<T> list : lists) {
            result.addAll(list);
        }
        return result;
    }

    public static <T> boolean rectangular(ArrayList<ArrayList<T>> lists) {
        return pairwiseStream(lists).allMatch(p -> p.a.size() == p.b.size());
    }

    public static <T> ArrayList<T> fill(T elem, int size) {
        final ArrayList<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(elem);
        }
        return result;
    }

    // == Pairwise. ====================================================================

    public static record Pair<T>(T a, T b) {}

    /**
        Same as Python's `itertools.pairwise`.
     */
    public static <T> Iterable<Pair<T>> pairwise(final Iterable<T> items) {
        return () -> new Pairwise<>(items);
    }

    public static <T> Stream<Pair<T>> pairwiseStream(final Iterable<T> items) {
        return StreamSupport.stream(pairwise(items).spliterator(), false);
    }

    private static final class Pairwise<T> implements Iterator<Pair<T>> {

        private final Iterator<T> iterator;
        private T a; // Pair.a, the first of its two items.

        Pairwise(final Iterable<T> items) {
            this.iterator = items.iterator();
            if (this.iterator.hasNext()) {
                this.a = this.iterator.next();
            }
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Pair<T> next() {
            final T b = iterator.next();
            final Pair<T> pair = new Pair<>(a, b);
            a = b;
            return pair;
        }
    }
}
