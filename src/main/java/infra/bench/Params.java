package infra.bench;

import infra.logging.Logger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import think.domain.model.Puzzle;
import think.manager.Manager;
import think.manager.Proposal;
import think.solvers.SolverKind;

/**
    Run many trials of a benchmark for a set of solvers, and print output as csv.
 */
// TODO: use nanoTime instead of currentTimeMillis
public record Params(List<SolverKind> solverKinds, Puzzle puzzle, long durationMillis, int trials) {
    private static final String SEPARATOR = ",";

    public Params {
        if (solverKinds.isEmpty() || durationMillis <= 0 || trials <= 0) {
            throw new IllegalArgumentException();
        }
    }

    // createReport: BiFunction<trialStartTimeMillis, proposalsProducedThisTrial, reportRecord>
    public <R extends Record> void execute(final BiFunction<Long, List<Proposal>, R> createReport) {
        final Map<SolverKind, List<R>> resultsByKind = new HashMap<>(solverKinds.size());
        for (final SolverKind kind : solverKinds) {
            resultsByKind.put(kind, new ArrayList<>(trials));
        }
        // Run in trial-major order (instead of solver-major order) to slightly reduce environment
        // and JIT effects. But it shouldn't matter much and we're not even bothering with JMH.
        for (int trial = 0; trial < trials; trial += 1) {
            for (final SolverKind kind : solverKinds) {
                try (Manager manager = new Manager(List.of(kind))) {
                    final long startTimeMillis = System.currentTimeMillis();
                    manager.solve(puzzle);
                    final List<Proposal> proposals = manager.consumeUntil(durationMillis);
                    final R result = createReport.apply(startTimeMillis, proposals);
                    resultsByKind.get(kind).add(result);
                }
            }
        }

        final RecordComponent[] components = resultsByKind
            .get(solverKinds.getFirst())
            .getFirst()
            .getClass()
            .getRecordComponents();

        final StringBuilder header = new StringBuilder();
        header.append("solver");
        for (final RecordComponent component : components) {
            header.append(SEPARATOR);
            header.append(component.getName());
        }
        Logger.results(header.toString());

        for (final SolverKind kind : solverKinds) {
            for (final R result : resultsByKind.get(kind)) {
                final StringBuilder row = new StringBuilder();
                final String solverName = kind.name().toLowerCase();
                row.append(solverName);
                for (final RecordComponent component : components) {
                    row.append(SEPARATOR);
                    try {
                        row.append(component.getAccessor().invoke(result));
                    } catch (IllegalAccessException | InvocationTargetException oops) {
                        throw new AssertionError("this always works when same module", oops);
                    }
                }
                Logger.results(row.toString());
            }
        }
    }
}
