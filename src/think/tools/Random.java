package think.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Stream;
import think.tools.Structures.Weighted;

/**
    Access randomization only through this class, think.tools.Random. Although nothing
    bad will happen, it is a design error to use other randoms like java.util.Random.

    It is undefined behavior to mutate the backing structure while any method from this
    class runs. Methods from this class will never mutate the backing structure.
 */
public final class Random {

    private Random() {}

    public static <T> T uniformChoice(final ArrayList<T> items) {
        return items.get(getRandom().nextInt(items.size()));
    }

    /**
        Stream items in a random order proportional to weight, without replacement.
     */
    public static <T> Stream<T> weightedStream(final ArrayList<Weighted<T>> weighteds) {
        assert weighteds.stream().allMatch(weighted -> weighted.weight() > 0);

        // https://en.wikipedia.org/wiki/Reservoir_sampling#Algorithm_A-Res
        // Modified with logarithm for numerical stability and to be lazy.

        final Function<Weighted<T>, Weighted<T>> reweight = item -> {
            final double random = getRandom().nextDouble();
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
        for (int index = 0; index < size; index += 1) {
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
                final int choice = getRandom().nextInt(remaining);
                final int index = indices.get(choice);
                remaining -= 1;
                indices.set(choice, indices.get(remaining));
                return items.get(index);
            }
        };
        return Iteration.toStream(iterator);
    }

    /**
        Sample an "element" from range 0 (inclusive) to "limit" (inclusive) with weight
        "population" choose "element". This implementation is approximate.
     */
    public static final class RestrictedBinomialDistribution {

        private final ArrayList<Double> cumulativeDistribution;

        public RestrictedBinomialDistribution(final int population, final int limit) {
            assert population >= limit;
            if (population > 100000) {
                Logging.log(getClass(), "I did not have this in mind.");
            }
            this.cumulativeDistribution = build(population, limit);
        }

        public int sample() {
            if (cumulativeDistribution.isEmpty()) {
                return 0;
            }
            final double total = cumulativeDistribution.getLast();
            // Binary search returns "(-(insertion point) - 1)" if the value is not
            // found. Since a double has many bits, it will never be found.
            final int index = -Collections.binarySearch(
                cumulativeDistribution,
                getRandom().nextDouble() * total
            );
            return index - 1;
        }

        private static ArrayList<Double> build(final int population, final int limit) {
            final ArrayList<Double> rawLogProbs = new ArrayList<>(limit + 1);
            for (int element = 0; element <= limit; element += 1) {
                rawLogProbs.add(logBinom(population, element));
            }
            final double largest = rawLogProbs.stream().reduce(0.0, Math::max);
            final ArrayList<Double> normLogProbs = new ArrayList<>(limit + 1);
            for (int element = 0; element <= limit; element += 1) {
                normLogProbs.add(rawLogProbs.get(element) - largest);
            }
            final ArrayList<Double> cumulativeDistribution = new ArrayList<>(limit + 1);
            double sum = 0.0;
            for (int element = 0; element <= limit; element += 1) {
                sum += Math.exp(normLogProbs.get(element));
                cumulativeDistribution.add(sum);
            }
            return cumulativeDistribution;
        }

        private static double logBinom(final int population, final int element) {
            // There is 1 way to pick none or all of the elements from a population.
            if (element == 0 || element == population) {
                return Math.log(1.0);
            }
            // A binomial coefficient n choose k is calculated n! / k! (n - k)!. Since
            // Stirling's approximation works on factorials, it also works on binomial
            // coefficients.
            // https://math.stackexchange.com/questions/64716/approximating-the-logarithm-of-the-binomial-coefficient
            return (
                population * Math.log(population) -
                element * Math.log(element) -
                (population - element) * Math.log(population - element)
            );
        }
    }

    private static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }
}
