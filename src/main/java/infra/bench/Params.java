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
            final long startTimeMillis = System.currentTimeMillis();
            final long endTimeMillis = startTimeMillis + durationMillis;
            while (System.currentTimeMillis() < endTimeMillis) {
                final long batchElapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
                final List<Proposal> batch = manager.consumeNow();
                batch.forEach(proposal -> uponProposal.accept(proposal, batchElapsedTimeMillis));
            }
        }
        report.run();
    }
}
