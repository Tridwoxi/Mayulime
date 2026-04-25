package infra.bench;

import java.util.ArrayList;
import java.util.List;
import think.ints.IntList;
import think.manager.Proposal;

public final class Timeline {

    private static final long BUCKET_MILLIS = 100L;

    public record Report(long bucketMillis, int count) {}

    public static final class Context {

        private final long startTimeNanos = System.nanoTime();
        private final IntList counts = new IntList(0);
    }

    private Timeline() {}

    public static Context initialContext() {
        return new Context();
    }

    public static Context reduce(final Context context, final Proposal proposal) {
        final long elapsedMillis =
            (proposal.getCreatedAtNanos() - context.startTimeNanos) / 1_000_000L;
        final int bucket = (int) (elapsedMillis / BUCKET_MILLIS);
        while (context.counts.size() <= bucket) {
            context.counts.add(0);
        }
        context.counts.set(bucket, context.counts.get(bucket) + 1);
        return context;
    }

    public static List<Report> createReports(final Context context) {
        if (context.counts.isEmpty()) {
            return List.of();
        }
        final List<Report> reports = new ArrayList<>(context.counts.size());
        for (int bucket = 0; bucket < context.counts.size(); bucket += 1) {
            reports.add(new Report(bucket * BUCKET_MILLIS, context.counts.get(bucket)));
        }
        return reports;
    }
}
