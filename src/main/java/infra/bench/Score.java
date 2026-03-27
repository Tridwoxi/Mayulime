package infra.bench;

import infra.output.Logging;
import think.domain.codec.Serializer;
import think.manager.Proposal;

public final class Score implements Runnable {

    private final Params params;
    private Proposal best;
    private long bestElapsedMs;

    public Score(final Params params) {
        this.params = params;
    }

    @Override
    public void run() {
        params.execute(this::accept, this::report);
    }

    private void accept(final Proposal proposal, final long elapsedMs) {
        if (best == null || proposal.getScore() > best.getScore()) {
            best = proposal;
            bestElapsedMs = elapsedMs;
        }
    }

    private void report() {
        if (best != null) {
            final String mapCode = Serializer.serialize(params.puzzle(), best.getFeatures());
            Logging.results("Solution: %s", mapCode);
            Logging.results("Score: %d", best.getScore());
            Logging.results("Found after: %d ms", bestElapsedMs);
        } else {
            Logging.results("Nothing found.");
        }
    }
}
