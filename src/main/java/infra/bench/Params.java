package infra.bench;

import infra.logging.Logger;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;
import think.domain.model.Puzzle;
import think.manager.Manager;
import think.manager.Proposal;
import think.solvers.SolverKind;

/**
    Benchmark configuration and shared lifecycle.

    {@link #execute} handles Manager creation, timing, and filtering out proposals that arrive
    outside the benchmark window. Callers supply what to track and what to log.
 */
public record Params(SolverKind solverKind, Puzzle puzzle, long durationMs, int parallelism) {
    public void execute(final ObjLongConsumer<Proposal> accept, final Runnable report) {
        final long[] startTimeMs = new long[1];
        final Consumer<Proposal> listener = proposal -> {
            final long elapsed = proposal.getCreatedAtMs() - startTimeMs[0];
            if (elapsed >= 0 && elapsed < durationMs) {
                accept.accept(proposal, elapsed);
            }
        };
        final List<SolverKind> solverKinds = Collections.nCopies(parallelism, solverKind);
        try (Manager manager = new Manager(listener, solverKinds)) {
            startTimeMs[0] = System.currentTimeMillis();
            manager.solve(puzzle);
            try {
                Thread.sleep(durationMs);
            } catch (InterruptedException shouldNotHappen) {
                Logger.warning("%s", shouldNotHappen.toString());
            }
            manager.stop();
        }
        report.run();
    }
}
