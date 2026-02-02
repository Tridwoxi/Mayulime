package think.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Stream;
import think.tools.Structures.Weighted;

/**
    Access randomization only through this class, think.stra.Random. Although nothing
    bad will happen, it is a design error to use other randoms like java.util.Random.

    It is undefined behavior to mutate the backing structure while any method from this
    class runs. Methods from this class will never mutate the backing structure.
 */
public final class Random {

    private Random() {}

    /**
        Stream items in a random order proportional to weight, without replacement.
     */
    public static <T> Stream<T> weightedStream(final ArrayList<Weighted<T>> weighteds) {
        assert weighteds.stream().allMatch(weighted -> weighted.weight() > 0);

        // https://en.wikipedia.org/wiki/Reservoir_sampling#Algorithm_A-Res
        // Modified with logarithm for numerical stability and to be lazy.

        final Function<Weighted<T>, Weighted<T>> reweight = item -> {
            final double random = ThreadLocalRandom.current().nextDouble();
            return new Weighted<>(item.item(), -Math.log(random) / item.weight());
        };
        final PriorityQueue<Weighted<T>> queue = new PriorityQueue<>(weighteds.size());
        queue.addAll(weighteds.stream().map(reweight).toList());

        final Iterator<T> iterator = new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return !queue.isEmpty();
            }

            @Override
            public T next() {
                return queue.remove().item();
            }
        };
        return Iteration.toStream(iterator);
    }

    /**
        Stream items in a random order, without replacement.
     */
    public static <T> Stream<T> uniformStream(final ArrayList<T> items) {
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
