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
        final int size = lists
            .stream()
            .mapToInt(x -> x.size())
            .sum();
        final ArrayList<T> result = new ArrayList<>(size);
        for (ArrayList<T> list : lists) {
            result.addAll(list);
        }
        assert result.size() == size;
        return result;
    }

    public static <T> boolean rectangular(ArrayList<ArrayList<T>> lists) {
        return pairwiseStream(lists).allMatch(p -> p.a.size() == p.b.size());
    }

    public static <T> ArrayList<T> fill(T item, int size) {
        final ArrayList<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(item);
        }
        assert result.size() == size;
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

        public Pairwise(final Iterable<T> items) {
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

    // == Ordering. ====================================================================

    public static record UniOrdered<T>(
        T item,
        int order1
    ) implements Comparable<UniOrdered<T>> {
        @Override
        public int compareTo(final UniOrdered<T> other) {
            return Integer.compare(order1, other.order1);
        }
    }

    public static record BiOrdered<T>(
        T item,
        int order1,
        int order2
    ) implements Comparable<BiOrdered<T>> {
        @Override
        public int compareTo(final BiOrdered<T> other) {
            final int cmpPriority = Integer.compare(order1, other.order1);
            if (cmpPriority != 0) {
                return cmpPriority;
            }
            return Integer.compare(order2, other.order2);
        }
    }
}
