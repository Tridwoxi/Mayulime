package infra.bench;

import java.util.Collections;
import java.util.List;
import java.util.function.ObjLongConsumer;
import think.domain.model.Puzzle;
import think.manager.Manager;
import think.manager.Proposal;
import think.solvers.SolverKind;

/**
    Benchmark configuration and shared lifecycle.
 */
public record Params(SolverKind solverKind, Puzzle puzzle, long durationMillis, int parallelism) {
    public void execute(final ObjLongConsumer<Proposal> uponProposal, final Runnable report) {
        try (Manager manager = new Manager(Collections.nCopies(parallelism, solverKind))) {
            manager.solve(puzzle);
            final long startTimeNanos = System.nanoTime();
            final long endTimeNanos = startTimeNanos + durationMillis * 1_000_000L;
            while (System.nanoTime() < endTimeNanos) {
                final long batchElapsedTimeMillis =
                    (System.nanoTime() - startTimeNanos) / 1_000_000L;
                final List<Proposal> batch = manager.consumeNow();
                batch.forEach(proposal -> uponProposal.accept(proposal, batchElapsedTimeMillis));
            }
        }
        report.run();
    }
}
