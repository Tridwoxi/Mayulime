package infra.bench;

import infra.launch.Bench.Params;
import infra.output.Logging;
import java.util.List;
import think.manager.Manager;
import think.manager.Manager.Proposal;

public final class Throughput implements Runnable {

    private final Params params;
    private volatile long startTimeMs;
    private volatile long numProposals;

    public Throughput(final Params params) {
        this.params = params;
        this.startTimeMs = 0L;
        this.numProposals = 0L;
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
        final double rate = ((double) numProposals / params.durationMs()) * 1000.0;
        Logging.results("Saw %d proposals in %d ms", numProposals, params.durationMs());
        Logging.results("That is %f per second", rate);
    }

    private void process(final Proposal proposal) {
        if (proposal.createdAtMs() - startTimeMs <= params.durationMs()) {
            numProposals += 1;
        }
    }
}
