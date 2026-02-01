package think.tools;

import java.util.HashMap;
import java.util.stream.Stream;
import think.tools.Ordering.UniOrdered;

/**
    Custom data structure implementations.
 */
public class Structures {

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
        public Stream<UniOrdered<T>> stream() {
            return counts
                .entrySet()
                .stream()
                .map(entry -> new UniOrdered<>(entry.getKey(), entry.getValue()));
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
