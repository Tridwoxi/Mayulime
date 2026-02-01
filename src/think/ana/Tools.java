package think.ana;

import java.util.LinkedHashMap;
import java.util.stream.Stream;

/**
    General purpose utility functions. Tpai-specific code must go in its own class.
 */
public final class Tools {

    private Tools() {}

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
