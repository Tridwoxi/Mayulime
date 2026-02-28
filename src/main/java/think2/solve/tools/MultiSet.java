package think2.solve.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
    Counts number of occurrences of items.
 */
public final class MultiSet<T> {

    public record Counted<T>(T item, int count) {}

    private final Map<T, Integer> counts;

    public MultiSet() {
        this.counts = new HashMap<>();
    }

    public MultiSet(final Collection<T> items) {
        this.counts = new HashMap<>();
        items.forEach(item -> counts.merge(item, 1, Integer::sum));
    }

    public boolean containsItem(final T item) {
        return counts.containsKey(item);
    }

    public int howManyOf(final T item) {
        return counts.getOrDefault(item, 0);
    }

    public Stream<Counted<T>> stream() {
        return counts
            .entrySet()
            .stream()
            .map(entry -> new Counted<>(entry.getKey(), entry.getValue()));
    }

    public int total() {
        return counts.values().stream().reduce(0, Integer::sum);
    }

    public void addAll(final MultiSet<T> other) {
        if (this == other) {
            throw new IllegalArgumentException();
        }
        other.counts.forEach((item, count) -> this.counts.merge(item, count, Integer::sum));
    }
}
