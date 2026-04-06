package infra.bench;

import infra.logging.Logger;
import java.util.Map.Entry;
import java.util.TreeMap;
import think.manager.Proposal;

public final class Distribution implements Runnable {

    private final Params params;
    private final TreeMap<Integer, Long> counts;

    public Distribution(final Params params) {
        this.params = params;
        this.counts = new TreeMap<>();
    }

    @Override
    public void run() {
        params.execute(this::accept, this::report);
    }

    private void accept(final Proposal proposal, final long elapsedMs) {
        final int score = proposal.getScore();
        counts.merge(score, 1L, Long::sum);
    }

    private void report() {
        Logger.results("score,count");
        for (final Entry<Integer, Long> entry : counts.entrySet()) {
            Logger.results("%d,%d", entry.getKey(), entry.getValue());
        }
    }
}
