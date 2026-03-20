package infra.bench;

import infra.launch.Bench.Params;
import infra.output.Logging;
import java.util.List;
import java.util.function.Consumer;
import think.domain.codec.Serializer;
import think.manager.Manager;
import think.manager.Manager.Proposal;

public final class Score implements Consumer<Params> {

    public Score() {}

    @Override
    public void accept(final Params params) {
        final Proposal[] best = new Proposal[] { null };
        final long[] startTimeMs = new long[] { 0L };

        final Consumer<Proposal> listener = statusUpdate -> {
            final boolean better = best[0] == null || statusUpdate.score() > best[0].score();
            final boolean legal = statusUpdate.createdAtMs() - startTimeMs[0] < params.durationMs();
            if (better && legal) {
                best[0] = statusUpdate;
            }
        };

        try (Manager manager = new Manager(listener, List.of(params.solverKind()))) {
            startTimeMs[0] = System.currentTimeMillis();
            manager.solve(params.puzzle());
            try {
                Thread.sleep(params.durationMs());
            } catch (InterruptedException exception) {
                Logging.warning("%s", exception.toString());
            }
            manager.stop();
        }

        if (best[0] != null) {
            final String mapCode = Serializer.serialize(params.puzzle(), best[0].features());
            final long elapsed = best[0].createdAtMs() - startTimeMs[0];
            Logging.results("Solution: %s", mapCode);
            Logging.results("Score: %d", best[0].score());
            Logging.results("Found after: %d ms", elapsed);
        } else {
            Logging.results("Nothing found.");
        }
    }
}
