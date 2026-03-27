package infra.bench;

import infra.output.Logging;
import think.manager.Proposal;

public final class Throughput implements Runnable {

    private final Params params;
    private long numProposals;

    public Throughput(final Params params) {
        this.params = params;
    }

    @Override
    public void run() {
        params.execute(this::accept, this::report);
    }

    private void accept(final Proposal proposal, final long elapsedMs) {
        numProposals += 1L;
    }

    private void report() {
        final double rate = ((double) numProposals / params.durationMs()) * 1000.0;
        Logging.results("Saw %d proposals in %d ms", numProposals, params.durationMs());
        Logging.results("That is %f per second", rate);
    }
}
