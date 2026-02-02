package think.tools;

import java.util.HashMap;
import java.util.stream.Stream;

/**
    Custom data structure implementations.
 */
public final class Structures {

    private Structures() {}

    /**
        Association between two items.
     */
    public record Pair<F, S>(F first, S second) {}

    /**
        Association between an item and a double. Comparable on the double.
     */
    public record Weighted<T>(T item, double weight) implements Comparable<Weighted<T>> {
        @Override
        public int compareTo(final Weighted<T> other) {
            return Double.compare(this.weight, other.weight);
        }
    }

    /**
        Association between an item an integer. Comparable on the integer.
     */
    public record Ordered<T>(T item, int order) implements Comparable<Ordered<T>> {
        @Override
        public int compareTo(final Ordered<T> other) {
            return Integer.compare(this.order, other.order);
        }
    }

    /**
        Counts number of occurrences of items.
     */
    public static final class MultiSet<T> {

        private final HashMap<T, Integer> counts;

        public MultiSet() {
            this.counts = new HashMap<>();
        }

        public MultiSet(final Iterable<T> items) {
            this.counts = new HashMap<>();
            items.forEach(item -> counts.merge(item, 1, Integer::sum));
        }

        public boolean containsItem(final T item) {
            return counts.containsKey(item);
        }

        public int howManyOf(final T item) {
            return counts.getOrDefault(item, 0);
        }

        /**
            Iteration order is arbitrary.
         */
        public Stream<Ordered<T>> stream() {
            return counts
                .entrySet()
                .stream()
                .map(entry -> new Ordered<>(entry.getKey(), entry.getValue()));
        }

        public int total() {
            // Potential optimization: If this method is called repeatedly, keep sum as
            // a field to avoid recomputation.
            return counts.values().stream().reduce(0, Integer::sum);
        }

        /**
            Add all elements from another MultiSet to this one, modifying this one.
         */
        public void addAll(final MultiSet<T> other) {
            assert this != other;
            other.counts.forEach((item, count) ->
                this.counts.merge(item, count, Integer::sum)
            );
        }
    }
}
