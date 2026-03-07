package think.solvers.naive;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
    Sample {@code element} from range {@code 0} (inclusive) to {@code limit} (inclusive) with weight
    {@code population choose element}. This implementation is approximate.
 */
final class RestrictedBinomial {

    private final double[] cumulativeDistribution;

    RestrictedBinomial(final int population, final int limit) {
        if (population < limit) {
            throw new IllegalArgumentException();
        }
        this.cumulativeDistribution = build(population, limit);
    }

    int sample() {
        if (cumulativeDistribution.length == 0) {
            return 0;
        }
        final double total = cumulativeDistribution[cumulativeDistribution.length - 1];
        // Binary search returns "(-(insertion point) - 1)" if the value is not found. Since a
        // double has many bits, it will never be found.
        final int index = -Arrays.binarySearch(
            cumulativeDistribution,
            ThreadLocalRandom.current().nextDouble() * total
        );
        return index - 1;
    }

    private static double[] build(final int population, final int limit) {
        final double[] rawLogProbs = new double[limit + 1];
        double largest = 0.0;
        for (int element = 0; element <= limit; element += 1) {
            rawLogProbs[element] = logBinom(population, element);
            largest = Math.max(largest, rawLogProbs[element]);
        }
        final double[] cumulativeDistribution = new double[limit + 1];
        for (int element = 0; element <= limit; element += 1) {
            final double previous = (element == 0) ? 0.0 : cumulativeDistribution[element - 1];
            cumulativeDistribution[element] = previous + Math.exp(rawLogProbs[element] - largest);
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
