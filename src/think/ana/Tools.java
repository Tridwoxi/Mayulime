package think.ana;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import think.tools.Ordering.UniOrdered;

/**
    General purpose utility functions. Tpai-specific code must go in its own class.
 */
public final class Tools {

    private Tools() {}

    // == List manipulation. ===========================================================

    public static <T> ArrayList<T> flatten(final ArrayList<ArrayList<T>> list2d) {
        final int size = list2d
            .stream()
            .mapToInt(list1d -> list1d.size())
            .sum();
        final ArrayList<T> result = new ArrayList<>(size);
        for (ArrayList<T> list1d : list2d) {
            result.addAll(list1d);
        }
        assert result.size() == size;
        return result;
    }

    public static <T> Stream<UniOrdered<T>> enumerate(final ArrayList<T> list) {
        return IntStream.range(0, list.size()).mapToObj(index ->
            new UniOrdered<>(list.get(index), index)
        );
    }

    private static <T> Stream<T> toStream(final Iterator<T> iterator) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED + Spliterator.NONNULL
            ),
            false
        );
    }

    // == Randoms. =====================================================================

    /**
        Lazily stream items in a random order. Does not modify underlying list, but
        requires it to not mutate.
     */
    public static <T> Stream<T> randomly(final ArrayList<T> items) {
        final int size = items.size();
        final ArrayList<Integer> indices = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            indices.add(index);
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
                final int index = indices.get(choice);
                remaining -= 1;
                indices.set(choice, indices.get(remaining));
                return items.get(index);
            }
        };
        return toStream(iterator);
    }

    // == Pairwise. ====================================================================

    public record Pair<F, S>(F first, S second) {}

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

        public void addAll(final Counter<T> other) {
            assert this != other;
            other.counts.forEach((item, count) ->
                this.counts.merge(item, count, Integer::sum)
            );
        }
    }
}
