package infra.bench;

import infra.logging.Logger;
import think.manager.Proposal;

public final class Timeline implements Runnable {

    private static final long BUCKET_MS = 100L;

    private final Params params;
    private final int numBuckets;
    private final int[] counts;

    public Timeline(final Params params) {
        this.params = params;
        this.numBuckets = (int) ((params.durationMs() + BUCKET_MS - 1) / BUCKET_MS);
        this.counts = new int[numBuckets];
    }

    @Override
    public void run() {
        params.execute(this::accept, this::report);
    }

    private void accept(final Proposal proposal, final long elapsedMs) {
        final int bucket = (int) (elapsedMs / BUCKET_MS);
        counts[bucket] += 1;
    }

    private void report() {
        Logger.results("bucket_ms,proposals");
        for (int bucket = 0; bucket < numBuckets; bucket += 1) {
            final long bucketStart = bucket * BUCKET_MS;
            Logger.results("%d,%d", bucketStart, counts[bucket]);
        }
    }
}
