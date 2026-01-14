package think.ana;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Spliterators;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
    General purpose utility functions. Tpai-specific code must go in its own class.
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
        return pairwise(lists).allMatch(p -> p.a.size() == p.b.size());
    }

    public static <T> ArrayList<T> fill(T item, int size) {
        final ArrayList<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(item);
        }
        assert result.size() == size;
        return result;
    }

    // == Randoms. =====================================================================

    /**
        Lazily stream items in a random order. It is undefined behavior to modify the
        underlying list.
     */
    public static <T> Stream<T> randomly(final ArrayList<T> items) {
        final int size = items.size();
        final int[] indices = new int[size];
        for (int i = 0; i < size; i++) {
            indices[i] = i;
        }
        final Iterator<T> iterator = new Iterator<>() {
            private int remaining = size;

            @Override
            public boolean hasNext() {
                return remaining > 0;
            }

            @Override
            public T next() {
                final int choice = ThreadLocalRandom.current().nextInt(remaining);
                final int index = indices[choice];
                remaining -= 1;
                indices[choice] = indices[remaining];
                return items.get(index);
            }
        };
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, 0),
            false
        );
    }

    // == Pairwise. ====================================================================

    public static record Pair<T>(T a, T b) {}

    /**
        Same as Python's `itertools.pairwise`.
     */
    public static <T> Stream<Pair<T>> pairwise(final Iterable<T> items) {
        final Iterator<T> source = items.iterator();
        if (!source.hasNext()) {
            return Stream.empty();
        }
        final T first = source.next();
        final Iterator<Pair<T>> iterator = new Iterator<>() {
            private T previous = first;

            @Override
            public boolean hasNext() {
                return source.hasNext();
            }

            @Override
            public Pair<T> next() {
                final T next = source.next();
                final Pair<T> pair = new Pair<>(previous, next);
                previous = next;
                return pair;
            }
        };
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, 0),
            false
        );
    }

    // == Ordering. ====================================================================

    public static record UniOrdered<T>(
        T item,
        int order1
    ) implements Comparable<UniOrdered<T>> {
        @Override
        public int compareTo(final UniOrdered<T> other) {
            return Integer.compare(this.order1, other.order1);
        }
    }

    public static record BiOrdered<T>(
        T item,
        int order1,
        int order2
    ) implements Comparable<BiOrdered<T>> {
        @Override
        public int compareTo(final BiOrdered<T> other) {
            final int first = Integer.compare(this.order1, other.order1);
            if (first != 0) {
                return first;
            }
            return Integer.compare(this.order2, other.order2);
        }
    }

    // == Counter. =====================================================================

    /**
        Counts number of occurrences of items in an iterable. Preserves insertion order.
     */
    public static final class Counter<T> {

        private final LinkedHashMap<T, Integer> counts;

        public Counter() {
            this.counts = new LinkedHashMap<>();
        }

        public Counter(final Iterable<T> items) {
            this.counts = new LinkedHashMap<>();
            items.forEach(item -> counts.merge(item, 1, Integer::sum));
        }

        public boolean containsItem(final T item) {
            return counts.containsKey(item);
        }

        public int getCount(final T item) {
            return counts.getOrDefault(item, 0);
        }

        public Stream<T> itemStream() {
            return counts.keySet().stream();
        }

        public int totalCount() {
            // Potential optimization: If this method is called repeatedly, keep sum as
            // a field to avoid recomputation.
            return counts.values().stream().reduce(0, Integer::sum);
        }

        public void addAll(Counter<T> other) {
            assert this != other;
            other.counts.forEach((item, count) ->
                this.counts.merge(item, count, Integer::sum)
            );
        }
    }
}
