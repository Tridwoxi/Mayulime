package infra.bench;

import java.util.ArrayList;
import java.util.List;
import think.manager.Proposal;

public final class Timeline {

    private static final long BUCKET_MILLIS = 100L;

    public record Report(long bucketMillis, int count) {}

    private Timeline() {}

    public static List<Report> createReports(
        final long startTimeMillis,
        final List<Proposal> proposals
    ) {
        if (proposals.isEmpty()) {
            return List.of();
        }
        long maxElapsedMillis = 0L;
        for (final Proposal proposal : proposals) {
            final long elapsed = proposal.getCreatedAtMillis() - startTimeMillis;
            if (elapsed > maxElapsedMillis) {
                maxElapsedMillis = elapsed;
            }
        }
        final int numBuckets = (int) (maxElapsedMillis / BUCKET_MILLIS) + 1;
        final int[] counts = new int[numBuckets];
        for (final Proposal proposal : proposals) {
            final long elapsed = proposal.getCreatedAtMillis() - startTimeMillis;
            final int bucket = (int) (elapsed / BUCKET_MILLIS);
            counts[bucket] += 1;
        }
        final List<Report> reports = new ArrayList<>(numBuckets);
        for (int bucket = 0; bucket < numBuckets; bucket += 1) {
            reports.add(new Report(bucket * BUCKET_MILLIS, counts[bucket]));
        }
        return reports;
    }
}
