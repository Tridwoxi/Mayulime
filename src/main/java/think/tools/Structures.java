package think.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
    Custom data structure implementations.
 */
public final class Structures {

    private Structures() {}

    public record Cell(int row, int col) {
        public int manhattanDistance(final Cell other) {
            return Math.abs(row - other.row) + Math.abs(col - other.col);
        }
    }

    public record Pair<F, S>(F first, S second) {}

    public record Weighted<T>(T item, double weight) implements Comparable<Weighted<T>> {
        @Override
        public int compareTo(final Weighted<T> other) {
            return Double.compare(this.weight, other.weight);
        }
    }

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
            // PERF: Cache to avoid recomputation.
            return counts.values().stream().reduce(0, Integer::sum);
        }

        /**
            Add all elements from another MultiSet to this one, modifying this one.
         */
        public void addAll(final MultiSet<T> other) {
            if (this == other) {
                throw new IllegalArgumentException();
            }
            other.counts.forEach((item, count) -> this.counts.merge(item, count, Integer::sum));
        }
    }

    public static final class RunningAverage {

        private long count;
        private double average;

        public RunningAverage() {
            this.count = 0L;
            this.average = 0.0;
        }

        public void insert(final double value) {
            this.count += 1L;
            this.average += (value - this.average) / this.count;
        }

        public double get() {
            return this.average;
        }
    }

    /**
        Specialized vector database to estimate the overlap of a vector.
     */
    public static final class DensityDB<T> {

        private static final int NUM_HASHES = 10;
        private final ArrayList<ArrayList<Integer>> permutations;
        private final int vectorSize;
        private final Function<T, ArrayList<Boolean>> converter;
        private final ArrayList<ArrayList<Integer>> buckets;
        private int numElements;

        // PERF: Use true bit vectors.
        public DensityDB(final int vectorSize, final Function<T, ArrayList<Boolean>> converter) {
            this.vectorSize = vectorSize;
            this.permutations = Iteration.filledArray(
                ignored -> Random.permutation(vectorSize),
                NUM_HASHES
            );
            this.converter = converter;
            this.buckets = Iteration.filledArray(
                ignored -> Iteration.filledArray(0, vectorSize),
                NUM_HASHES
            );
            this.numElements = 0;
        }

        public void insert(final T item) {
            final ArrayList<Integer> hash = minHash(item);
            IntStream.range(0, NUM_HASHES).forEachOrdered(index -> {
                final int current = buckets.get(index).get(hash.get(index));
                buckets.get(index).set(hash.get(index), current + 1);
            });
            numElements += 1;
        }

        public double estimate(final T item) {
            final ArrayList<Integer> hash = minHash(item);
            final double unnormalized = IntStream.range(0, NUM_HASHES)
                .mapToDouble(index -> buckets.get(index).get(hash.get(index)))
                .average()
                .orElse(0);
            return numElements == 0 ? 0 : unnormalized / (double) numElements;
        }

        private ArrayList<Integer> minHash(final T item) {
            // https://en.wikipedia.org/wiki/MinHash
            final ArrayList<Boolean> bitVector = converter.apply(item);
            if (bitVector.size() != vectorSize) {
                throw new IllegalArgumentException();
            }
            final Function<Integer, Integer> firstTrue = whichPermutation ->
                permutations
                    .get(whichPermutation)
                    .stream()
                    .filter(bitVector::get)
                    .findFirst()
                    .orElse(bitVector.size() - 1);
            return Iteration.filledArray(firstTrue, NUM_HASHES);
        }
    }
}
