package solvers.bruteforce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

/**
    Sample an "element" from range 0 (inclusive) to "limit" (inclusive) with weight
    "population" choose "element". This implementation is approximate.
 */
public final class RestrictedBinomial {

    private final ArrayList<Double> cumulativeDistribution;

    public RestrictedBinomial(final int population, final int limit) {
        if (population < limit) {
            throw new IllegalArgumentException();
        }
        this.cumulativeDistribution = build(population, limit);
    }

    public int sample() {
        if (cumulativeDistribution.isEmpty()) {
            return 0;
        }
        final double total = cumulativeDistribution.getLast();
        // Binary search returns "(-(insertion point) - 1)" if the value is not found. Since a
        // double has many bits, it will never be found.
        final int index = -Collections.binarySearch(
            cumulativeDistribution,
            ThreadLocalRandom.current().nextDouble() * total
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
        // A binomial coefficient n choose k is calculated n! / k! (n - k)!. Since Stirling's
        // approximation works on factorials, it also works on binomial coefficients.
        // https://math.stackexchange.com/questions/64716/approximating-the-logarithm-of-the-binomial-coefficient
        return (
            population * Math.log(population) -
            element * Math.log(element) -
            (population - element) * Math.log(population - element)
        );
    }
}
