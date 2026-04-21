package infra.bench;

import java.util.ArrayList;
import java.util.List;
import think.manager.Proposal;

public final class Latency {

    /**
       An inclusive [lowerMillis, upperMillis] bucket. Buckets are log2-scaled: {0}, {1}, [2,3],
       [4,7], [8,15], ... to keep the count manageable across the zero-to-minutes range.
     */
    public record Report(long lowerMillis, long upperMillis, int count) {}

    private Latency() {}

    public static List<Report> createReports(
        final long startTimeMillis,
        final List<Proposal> proposals
    ) {
        if (proposals.size() < 2) {
            return List.of();
        }
        int maxBucket = 0;
        final int[] counts = new int[Long.SIZE + 1];
        long lastMillis = proposals.getFirst().getCreatedAtMillis();
        for (int index = 1; index < proposals.size(); index += 1) {
            final long createdAtMillis = proposals.get(index).getCreatedAtMillis();
            final int bucket = bucketIndex(createdAtMillis - lastMillis);
            counts[bucket] += 1;
            if (bucket > maxBucket) {
                maxBucket = bucket;
            }
            lastMillis = createdAtMillis;
        }
        final List<Report> reports = new ArrayList<>(maxBucket + 1);
        for (int bucket = 0; bucket <= maxBucket; bucket += 1) {
            reports.add(new Report(bucketLower(bucket), bucketUpper(bucket), counts[bucket]));
        }
        return reports;
    }

    private static int bucketIndex(final long intervalMillis) {
        if (intervalMillis <= 0L) {
            return 0;
        }
        return Long.SIZE - Long.numberOfLeadingZeros(intervalMillis);
    }

    private static long bucketLower(final int bucket) {
        return bucket == 0 ? 0L : 1L << (bucket - 1);
    }

    private static long bucketUpper(final int bucket) {
        return bucket == 0 ? 0L : (1L << bucket) - 1L;
    }
}
