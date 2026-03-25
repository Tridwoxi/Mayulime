package infra.bench;

import infra.launch.Bench.Params;
import infra.output.Logging;
import think.manager.Manager;
import think.manager.Proposal;

public final class Agreement implements Runnable {

    private static final int UNSCORED = -2;
    private final Params params;
    private volatile long startTimeMs;
    private volatile long numProposals;
    private volatile long numBest;
    private volatile int topScore;

    public Agreement(final Params params) {
        this.params = params;
        this.startTimeMs = 0L;
        this.numProposals = 0L;
        this.numBest = 0L;
        this.topScore = UNSCORED;
    }

    @Override
    public void run() {
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

        final String topScoreText = topScore == UNSCORED ? "Unscored" : Integer.toString(topScore);
        final double fraction = numProposals == 0L ? 0.0 : (double) numBest / numProposals;
        Logging.results("Top score %s", topScoreText);
        Logging.results("Achieved by %d of %d proposals", numBest, numProposals);
        Logging.results("As a fraction, that is %f", fraction);
    }

    private void process(final Proposal proposal) {
        if (proposal.getCreatedAtMs() - startTimeMs > params.durationMs()) {
            return;
        }

        numProposals += 1L;
        if (topScore == UNSCORED || proposal.getScore() > topScore) {
            topScore = proposal.getScore();
            numBest = 1L;
            return;
        }
        if (proposal.getScore() == topScore) {
            numBest += 1L;
        }
    }
}
