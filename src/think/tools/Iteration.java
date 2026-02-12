package think.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import think.tools.Structures.Ordered;
import think.tools.Structures.Pair;

/**
    Iteration- and stream-related utilities.
 */
public final class Iteration {

    private Iteration() {}

    /**
        Create a list of the specified size filled with the given item.
     */
    public static <T> ArrayList<T> filledArray(final T item, final int size) {
        final ArrayList<T> result = new ArrayList<>(size);
        for (int index = 0; index < size; index += 1) {
            result.add(item);
        }
        return result;
    }

    /**
        Create a list of the specified size filled with the results of the converter
        applied to each index in range [0, size).
     */
    public static <T> ArrayList<T> filledArray(
        final Function<Integer, T> converter,
        final int size
    ) {
        final ArrayList<T> result = new ArrayList<>(size);
        for (int index = 0; index < size; index += 1) {
            result.add(converter.apply(index));
        }
        return result;
    }

    /**
        Stream the elements of the given list with their indices.
     */
    public static <T> Stream<Ordered<T>> enumerate(final ArrayList<T> list) {
        return IntStream.range(0, list.size()).mapToObj(index ->
            new Ordered<>(list.get(index), index)
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
        Collect a stream into an ArrayList.
     */
    public static <T> ArrayList<T> materialize(final Stream<T> stream) {
        return stream.collect(Collectors.toCollection(ArrayList::new));
    }

    static <T> Stream<T> toStream(final Iterator<T> iterator) {
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
