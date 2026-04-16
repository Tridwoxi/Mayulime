package infra.bench;

import infra.logging.Logger;
import think.domain.codec.Serializer;
import think.manager.Proposal;

public final class Score implements Runnable {

    private final Params params;
    private Proposal best;
    private long bestElapsedMillis;

    public Score(final Params params) {
        this.params = params;
    }

    @Override
    public void run() {
        params.execute(this::accept, this::report);
    }

    private void accept(final Proposal proposal, final long elapsedMillis) {
        if (best == null || proposal.getScore() > best.getScore()) {
            best = proposal;
            bestElapsedMillis = elapsedMillis;
        }
    }

    private void report() {
        if (best != null) {
            final String mapCode = Serializer.serialize(params.puzzle(), best.getState());
            Logger.results("Best proposal: %s", mapCode);
            Logger.results("Score: %d", best.getScore());
            Logger.results("Found after: %d ms", bestElapsedMillis);
        } else {
            Logger.results("Nothing found.");
        }
    }
}
