package infra.bench;

import infra.output.Logging;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import think.manager.Proposal;

public final class Latency implements Runnable {

    private final Params params;
    private final List<Long> intervals = new ArrayList<>();
    private long lastMs = -1L;

    public Latency(final Params params) {
        this.params = params;
    }

    @Override
    public void run() {
        params.execute(this::accept, this::report);
    }

    private void accept(final Proposal proposal, final long elapsedMs) {
        if (lastMs >= 0L) {
            intervals.add(elapsedMs - lastMs);
        }
        lastMs = elapsedMs;
    }

    private void report() {
        if (intervals.isEmpty()) {
            Logging.results("No intervals recorded.");
            return;
        }
        Collections.sort(intervals);
        final long median = intervals.get(intervals.size() / 2);
        final long min = intervals.getFirst();
        final long max = intervals.getLast();
        final long sum = intervals.stream().mapToLong(Long::longValue).sum();
        final double mean = (double) sum / intervals.size();
        final double variance =
            intervals
                .stream()
                .mapToDouble(interval -> (interval - mean) * (interval - mean))
                .sum() /
            intervals.size();
        final double stddev = Math.sqrt(variance);
        Logging.results("Proposals: %d, intervals: %d", intervals.size() + 1, intervals.size());
        Logging.results(
            "Median: %d ms, min: %d ms, max: %d ms, total: %d ms, stddev: %.1f ms",
            median,
            min,
            max,
            sum,
            stddev
        );
    }
}
