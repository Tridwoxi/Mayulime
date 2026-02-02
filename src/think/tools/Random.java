package think.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

/**
    Access randomization only through this class, think.stra.Random. Although nothing
    bad will happen, it is a design error to use other randoms like java.util.Random.

    It is undefined behavior to mutate the backing structure while any method from this
    class runs. Methods from this class will never mutate the backing structure.
 */
public final class Random {

    private Random() {}

    /**
        Stream items in a random order, without replacement.
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
        return Iteration.toStream(iterator);
    }
}
