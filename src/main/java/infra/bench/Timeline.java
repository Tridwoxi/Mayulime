package infra.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import think.ints.IntList;
import think.manager.Proposal;

public final class Timeline {

    private static final long BUCKET_MILLIS = 100L;

    public record Report(long bucketMillis, int count) {}

    private Timeline() {}

    public static List<Report> createReports(final Stream<Proposal> proposals) {
        final long startTimeNanos = System.nanoTime();
        final IntList counts = new IntList(0);
        for (final Proposal proposal : (Iterable<Proposal>) proposals::iterator) {
            final long elapsedMillis = (proposal.getCreatedAtNanos() - startTimeNanos) / 1_000_000L;
            final int bucket = (int) (elapsedMillis / BUCKET_MILLIS);
            while (counts.size() <= bucket) {
                counts.add(0);
            }
            counts.set(bucket, counts.get(bucket) + 1);
        }

        if (counts.isEmpty()) {
            return List.of();
        }
        final List<Report> reports = new ArrayList<>(counts.size());
        for (int bucket = 0; bucket < counts.size(); bucket += 1) {
            reports.add(new Report(bucket * BUCKET_MILLIS, counts.get(bucket)));
        }
        return reports;
    }
}
