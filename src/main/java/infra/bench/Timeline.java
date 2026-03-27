package infra.bench;

import infra.launch.Bench.Params;
import infra.output.Logging;
import think.manager.Manager;
import think.manager.Proposal;

public final class Timeline implements Runnable {

    private static final long BUCKET_MS = 100L;

    private final Params params;
    private volatile long startTimeMs;
    private int[] counts;

    public Timeline(final Params params) {
        this.params = params;
        this.startTimeMs = 0L;
    }

    @Override
    public void run() {
        final int numBuckets = (int) ((params.durationMs() + BUCKET_MS - 1) / BUCKET_MS);
        counts = new int[numBuckets];

        try (Manager manager = new Manager(this::process, params.buildSolverKinds())) {
            startTimeMs = System.currentTimeMillis();
            manager.solve(params.puzzle());
            try {
                Thread.sleep(params.durationMs());
            } catch (InterruptedException exception) {
                Logging.warning("%s", exception.toString());
            }
            manager.stop();
        }

        Logging.results("bucket_ms,proposals");
        for (int bucket = 0; bucket < numBuckets; bucket += 1) {
            final long bucketStart = bucket * BUCKET_MS;
            Logging.results("%d,%d", bucketStart, counts[bucket]);
        }
    }

    private void process(final Proposal proposal) {
        final long elapsed = proposal.getCreatedAtMs() - startTimeMs;
        if (elapsed < 0 || elapsed >= params.durationMs()) {
            return;
        }
        final int bucket = (int) (elapsed / BUCKET_MS);
        counts[bucket] += 1;
    }
}
