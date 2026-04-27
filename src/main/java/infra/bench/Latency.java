package infra.bench;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import think.manager.Proposal;

public final class Latency {

    /**
       An inclusive [lowerNanos, upperNanos] bucket. Buckets are log2-scaled: {0}, {1}, [2,3],
       [4,7], [8,15], ... to keep the count manageable across the zero-to-minutes range.
     */
    public record Report(long lowerNanos, long upperNanos, int count) {}

    private Latency() {}

    public static List<Report> createReports(final Stream<Proposal> proposals) {
        Duration lastCreatedAfter = Duration.ZERO;
        int proposalsSeen = 0;
        int maxBucket = 0;
        final int[] counts = new int[Long.SIZE + 1];

        for (final Proposal proposal : (Iterable<Proposal>) proposals::iterator) {
            final Duration createdAfter = proposal.getCreatedAfter();
            if (proposalsSeen >= 1) {
                final Duration interval = createdAfter.minus(lastCreatedAfter);
                final int bucket = bucketIndex(interval);
                counts[bucket] += 1;
                if (bucket > maxBucket) {
                    maxBucket = bucket;
                }
            }
            lastCreatedAfter = createdAfter;
            proposalsSeen += 1;
        }

        if (proposalsSeen <= 1) {
            return List.of();
        }
        final List<Report> reports = new ArrayList<>(maxBucket + 1);
        for (int bucket = 0; bucket <= maxBucket; bucket += 1) {
            reports.add(new Report(bucketLower(bucket), bucketUpper(bucket), counts[bucket]));
        }
        return reports;
    }

    private static int bucketIndex(final Duration interval) {
        if (!interval.isPositive()) {
            return 0;
        }
        return Long.SIZE - Long.numberOfLeadingZeros(interval.toNanos());
    }

    private static long bucketLower(final int bucket) {
        return bucket == 0 ? 0L : 1L << (bucket - 1);
    }

    private static long bucketUpper(final int bucket) {
        return bucket == 0 ? 0L : (1L << bucket) - 1L;
    }
}
