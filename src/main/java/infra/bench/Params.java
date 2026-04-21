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
    Run many trials of a benchmark for a set of solvers. Print output as CSV.
 */
// TODO: use nanoTime instead of currentTimeMillis
public record Params(List<SolverKind> solverKinds, Puzzle puzzle, long durationMillis, int trials) {
    private static final String SEPARATOR = ",";

    public Params {
        if (solverKinds.isEmpty() || durationMillis <= 0 || trials <= 0) {
            throw new IllegalArgumentException();
        }
    }

    /**
        {@code createReports(startTimeMillis, proposals) -> reportRecords}
     */
    public <R extends Record> void execute(
        final Class<R> reportClass,
        final BiFunction<Long, List<Proposal>, List<R>> createReports
    ) {
        final Map<SolverKind, List<TrialResult<R>>> reportsByKind = HashMap.newHashMap(
            solverKinds.size()
        );
        for (final SolverKind kind : solverKinds) {
            reportsByKind.put(kind, new ArrayList<>());
        }
        // Run in trial-major order (instead of solver-major order) to slightly reduce environment
        // and JIT effects. But it shouldn't matter much and we're not even bothering with JMH.
        for (int trial = 0; trial < trials; trial += 1) {
            for (final SolverKind solver : solverKinds) {
                try (Manager manager = new Manager(List.of(solver))) {
                    final long startTimeMillis = System.currentTimeMillis();
                    manager.solve(puzzle);
                    final List<Proposal> proposals = manager.consumeUntil(durationMillis);
                    final List<R> reports = createReports.apply(startTimeMillis, proposals);
                    for (final R report : reports) {
                        reportsByKind.get(solver).add(new TrialResult<>(trial, report));
                    }
                }
            }
        }

        final RecordComponent[] components = reportClass.getRecordComponents();

        final StringBuilder header = new StringBuilder();
        header.append("solver");
        for (final RecordComponent component : components) {
            header.append(SEPARATOR);
            header.append(component.getName());
        }
        Logger.results(header.toString());

        for (final SolverKind solver : solverKinds) {
            final String solverName = solver.name().toLowerCase();
            for (final TrialResult<R> result : reportsByKind.get(solver)) {
                final StringBuilder row = new StringBuilder();
                row.append(solverName);
                row.append(SEPARATOR);
                row.append(result.trial);
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

    private record TrialResult<R extends Record>(int trial, R result) {}
}
