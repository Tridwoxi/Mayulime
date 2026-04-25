package infra.bench;

import infra.logging.Logger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import think.domain.model.Puzzle;
import think.manager.Manager;
import think.manager.Proposal;
import think.solvers.SolverKind;

/**
    Run many trials of a benchmark for a set of solvers. Output tsv.
 */
public record Params(List<SolverKind> solverKinds, Puzzle puzzle, long durationMillis, int trials) {
    // It is easier to use tsv than it is to do csv escaping logic (both here and downstream).
    // Assume nobody ever passes a newline or tab.
    private static final String SEPARATOR = "\t";

    public Params {
        solverKinds = List.copyOf(solverKinds);
        if (solverKinds.isEmpty() || durationMillis <= 0 || trials <= 0) {
            throw new IllegalArgumentException();
        }
    }

    public <R extends Record> void runAsBatch(
        final Class<R> reportClass,
        final BiFunction<Long, List<Proposal>, List<R>> createReports
    ) {
        // High-throughput solvers with long runtimes (e.g. RandomSolver + 10 seconds) can OOM
        // under this batch API, but it is unlikely to be a problem.
        record Context(long startTimeNanos, List<Proposal> proposals) {}
        final Supplier<Context> initializeContext = () -> {
            return new Context(System.nanoTime(), new ArrayList<>());
        };
        final BiFunction<Context, Proposal, Context> reduceProposal = (context, proposal) -> {
            context.proposals.add(proposal);
            return context;
        };
        final Function<Context, List<R>> capture = context -> {
            return createReports.apply(context.startTimeNanos(), context.proposals());
        };
        runAsStream(reportClass, initializeContext, reduceProposal, capture);
    }

    public <C, R extends Record> void runAsStream(
        final Class<R> reportClass,
        final Supplier<C> initializeContext,
        final BiFunction<C, Proposal, C> reduce,
        final Function<C, List<R>> createReports
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
                    C context = initializeContext.get();
                    System.gc();
                    final long deadline = System.nanoTime() + durationMillis * 1_000_000;
                    manager.solve(puzzle);
                    while (System.nanoTime() - deadline < 0) {
                        final List<Proposal> proposals = manager.consumeNow();
                        if (proposals.size() == 0) {
                            try {
                                final long difference = (deadline - System.nanoTime());
                                final long sleepNanos = Math.max(0, Math.min(difference, 100_000));
                                Thread.sleep(0L, (int) sleepNanos);
                            } catch (InterruptedException exception) {
                                throw new AssertionError(exception);
                            }
                        }
                        for (final Proposal proposal : proposals) {
                            context = reduce.apply(context, proposal);
                        }
                    }
                    final List<R> reports = createReports.apply(context);
                    for (final R report : reports) {
                        reportsByKind.get(solver).add(new TrialResult<>(trial, report));
                    }
                }
            }
        }

        final RecordComponent[] components = reportClass.getRecordComponents();

        final StringBuilder header = new StringBuilder();
        header.append("solver");
        header.append(SEPARATOR);
        header.append("trial");
        for (final RecordComponent component : components) {
            header.append(SEPARATOR);
            header.append(component.getName());
        }
        Logger.results("%s", header);

        for (final SolverKind solver : solverKinds) {
            final String solverName = solver.name().toLowerCase();
            for (final TrialResult<R> result : reportsByKind.get(solver)) {
                final StringBuilder row = new StringBuilder();
                row.append(solverName);
                row.append(SEPARATOR);
                row.append(result.trial());
                for (final RecordComponent component : components) {
                    row.append(SEPARATOR);
                    try {
                        row.append(component.getAccessor().invoke(result.report()));
                    } catch (IllegalAccessException | InvocationTargetException oops) {
                        throw new AssertionError("record accessors are public", oops);
                    }
                }
                Logger.results("%s", row);
            }
        }
    }

    private record TrialResult<R extends Record>(int trial, R report) {}
}
