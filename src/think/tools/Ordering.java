package think.tools;

/**
    An ordering is an association between an item and one or more numbers. Orderings
    are compared by examining each number.
 */
public final class Ordering {

    private Ordering() {}

    public record UniOrdered<T>(
        T item,
        int order1
    ) implements Comparable<UniOrdered<T>> {
        @Override
        public int compareTo(final UniOrdered<T> other) {
            return Integer.compare(this.order1, other.order1);
        }
    }

    public record BiOrdered<T>(
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
}
