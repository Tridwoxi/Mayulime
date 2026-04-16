package infra.bench;

import infra.logging.Logger;
import think.manager.Proposal;

public final class Agreement implements Runnable {

    private final Params params;
    private long numProposals;
    private long numBest;
    private int topScore;
    private boolean scored;

    public Agreement(final Params params) {
        this.params = params;
        this.numProposals = 0L;
        this.numBest = 0L;
        this.topScore = 0;
        this.scored = false;
    }

    @Override
    public void run() {
        params.execute(this::accept, this::report);
    }

    private void accept(final Proposal proposal, final long elapsedMillis) {
        numProposals += 1L;
        if (!scored || proposal.getScore() > topScore) {
            topScore = proposal.getScore();
            numBest = 1L;
            scored = true;
        } else if (proposal.getScore() == topScore) {
            numBest += 1L;
        }
    }

    private void report() {
        final String topScoreText = scored ? Integer.toString(topScore) : "Unscored";
        final double fraction = numProposals == 0L ? 0.0 : (double) numBest / numProposals;
        Logger.results("Top score %s", topScoreText);
        Logger.results("Achieved by %d of %d proposals", numBest, numProposals);
        Logger.results("As a fraction, that is %f", fraction);
    }
}
