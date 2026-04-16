package infra.bench;

import infra.logging.Logger;
import think.manager.Proposal;

public final class Timeline implements Runnable {

    private static final long BUCKET_MILLIS = 100L;

    private final Params params;
    private final int numBuckets;
    private final int[] counts;

    public Timeline(final Params params) {
        this.params = params;
        this.numBuckets = (int) ((params.durationMillis() + BUCKET_MILLIS - 1) / BUCKET_MILLIS);
        this.counts = new int[numBuckets];
    }

    @Override
    public void run() {
        params.execute(this::accept, this::report);
    }

    private void accept(final Proposal proposal, final long elapsedMillis) {
        final int bucket = (int) (elapsedMillis / BUCKET_MILLIS);
        counts[bucket] += 1;
    }

    private void report() {
        Logger.results("bucket_ms,proposals");
        for (int bucket = 0; bucket < numBuckets; bucket += 1) {
            final long bucketStart = bucket * BUCKET_MILLIS;
            Logger.results("%d,%d", bucketStart, counts[bucket]);
        }
    }
}
