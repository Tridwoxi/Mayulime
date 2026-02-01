package think.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import think.tools.Ordering.UniOrdered;

/**
    Iteration- and stream-related utilities.
 */
public final class Iteration {

    public record Pair<F, S>(F first, S second) {}

    private Iteration() {}

    /**
        Create an array of the specified size filled with the given item.
     */
    public static <T> ArrayList<T> filledArray(final T item, final int size) {
        final ArrayList<T> result = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            result.add(item);
        }
        assert result.size() == size;
        return result;
    }

    /**
        Stream the elements of the given list with their indices.
     */
    public static <T> Stream<UniOrdered<T>> enumerate(final ArrayList<T> list) {
        return IntStream.range(0, list.size()).mapToObj(index ->
            new UniOrdered<>(list.get(index), index)
        );
    }

    /**
        Lazily stream two iterables at once.

        @throws
            NoSuchElementException if one iterable is exhausted before the other.
     */
    public static <F, S> Stream<Pair<F, S>> zip(
        final Iterable<F> firsts,
        final Iterable<S> seconds
    ) {
        final Iterator<F> firstIterator = firsts.iterator();
        final Iterator<S> secondIterator = seconds.iterator();

        final Iterator<Pair<F, S>> iterator = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return firstIterator.hasNext() || secondIterator.hasNext();
            }

            @Override
            public Pair<F, S> next() {
                return new Pair<>(firstIterator.next(), secondIterator.next());
            }
        };
        return toStream(iterator);
    }

    /**
        Lazily stream an iterable's item and its previous item at the same time.

        A pairwise stream of an iterable of length N contains N-1 items, unless the
        iterable is empty, in which case the stream is also empty.
     */
    public static <T> Stream<Pair<T, T>> pairwise(final Iterable<T> items) {
        final Iterator<T> source = items.iterator();
        if (!source.hasNext()) {
            return Stream.empty();
        }
        final T first = source.next();
        final Iterator<Pair<T, T>> iterator = new Iterator<>() {
            private T previous = first;

            @Override
            public boolean hasNext() {
                return source.hasNext();
            }

            @Override
            public Pair<T, T> next() {
                final T next = source.next();
                final Pair<T, T> pair = new Pair<>(previous, next);
                previous = next;
                return pair;
            }
        };
        return toStream(iterator);
    }

    /**
        Stream items in a random order.

        It is undefined behavior to mutate the underlying list while this method runs.
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

    private static <T> Stream<T> toStream(final Iterator<T> iterator) {
        // The stream is always ordered because all our streams come from lists. It is
        // always non-null because the project prohibits passing nulls.
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED + Spliterator.NONNULL
            ),
            false
        );
    }
}
