package infra.bench;

import infra.launch.Bench.Params;
import infra.output.Logging;
import java.util.List;
import think.domain.codec.Serializer;
import think.manager.Manager;
import think.manager.Proposal;

public final class Score implements Runnable {

    private final Params params;
    private volatile Proposal best;
    private volatile long startTimeMs;

    public Score(final Params params) {
        this.params = params;
        this.best = null;
        this.startTimeMs = 0L;
    }

    @Override
    public void run() {
        try (Manager manager = new Manager(this::process, List.of(params.solverKind()))) {
            startTimeMs = System.currentTimeMillis();
            manager.solve(params.puzzle());
            try {
                Thread.sleep(params.durationMs());
            } catch (InterruptedException exception) {
                Logging.warning("%s", exception.toString());
            }
            manager.stop();
        }

        if (best != null) {
            final String mapCode = Serializer.serialize(params.puzzle(), best.getFeatures());
            final long elapsed = best.getCreatedAtMs() - startTimeMs;
            Logging.results("Solution: %s", mapCode);
            Logging.results("Score: %d", best.getScore());
            Logging.results("Found after: %d ms", elapsed);
        } else {
            Logging.results("Nothing found.");
        }
    }

    private void process(final Proposal proposal) {
        final boolean preferred = best == null || proposal.getScore() > best.getScore();
        final boolean legal = proposal.getCreatedAtMs() - startTimeMs < params.durationMs();
        if (preferred && legal) {
            best = proposal;
        }
    }
}
