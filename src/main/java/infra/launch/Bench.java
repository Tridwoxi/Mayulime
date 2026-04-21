package infra.launch;

import infra.bench.Agreement;
import infra.bench.Distribution;
import infra.bench.Latency;
import infra.bench.Optimality;
import infra.bench.Params;
import infra.bench.Score;
import infra.bench.Throughput;
import infra.bench.Timeline;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.TypeConversionException;
import think.domain.codec.Parser;
import think.domain.model.Puzzle;
import think.solvers.SolverKind;

/**
    Development-only benchmark runner.
 */
@Command(name = "bench", version = "Mayulime 0.1.0", mixinStandardHelpOptions = true)
public final class Bench implements Runnable {

    @Parameters(
        paramLabel = "<benchKind>",
        description = "Name of benchmark to run, one of ${COMPLETION-CANDIDATES}."
    )
    private BenchKind benchKind;

    @Parameters(
        paramLabel = "<mapCodeFile>",
        description = "Path to the Pathery MapCode file.",
        converter = StringToPuzzle.class
    )
    private Puzzle mapCodeFile;

    @Parameters(
        paramLabel = "<durationMillis>",
        description = "How long to run benchmark for.",
        converter = StringToPositiveLong.class
    )
    private Long durationMillis;

    @Parameters(
        paramLabel = "<trials>",
        description = "How many times to run the benchmark.",
        converter = StringToPositiveInt.class
    )
    private Integer trials;

    @Parameters(
        paramLabel = "<solverKinds>",
        description = "Comma-separated list of solvers, each one of ${COMPLETION-CANDIDATES}",
        split = ","
    )
    private List<SolverKind> solverKinds;

    private Bench() {}

    @Override
    public void run() {
        final Params params = new Params(
            Objects.requireNonNull(solverKinds),
            Objects.requireNonNull(mapCodeFile),
            Objects.requireNonNull(durationMillis),
            Objects.requireNonNull(trials)
        );
        switch (Objects.requireNonNull(benchKind)) {
            case AGREEMENT -> params.execute(Agreement.Report.class, Agreement::createReports);
            case DISTRIBUTION -> params.execute(
                Distribution.Report.class,
                Distribution::createReports
            );
            case LATENCY -> params.execute(Latency.Report.class, Latency::createReports);
            case OPTIMALITY -> params.execute(Optimality.Report.class, Optimality::createReports);
            case SCORE -> params.execute(Score.Report.class, Score::createReports);
            case THROUGHPUT -> params.execute(Throughput.Report.class, Throughput::createReports);
            case TIMELINE -> params.execute(Timeline.Report.class, Timeline::createReports);
            default -> throw new AssertionError();
        }
    }

    public static void main(final String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((_, exception) -> {
            exception.printStackTrace();
            System.exit(1);
        });
        System.exit(
            new CommandLine(new Bench()).setCaseInsensitiveEnumValuesAllowed(true).execute(args)
        );
    }

    private enum BenchKind {
        AGREEMENT,
        DISTRIBUTION,
        LATENCY,
        OPTIMALITY,
        SCORE,
        THROUGHPUT,
        TIMELINE,
    }

    private static final class StringToPuzzle implements ITypeConverter<Puzzle> {

        @Override
        public Puzzle convert(final String value) throws Exception {
            return Parser.parse(Files.readString(Path.of(value)).strip());
        }
    }

    private static final class StringToPositiveLong implements ITypeConverter<Long> {

        @Override
        public Long convert(final String value) throws Exception {
            final long result = Long.parseLong(value);
            if (result <= 0L) {
                throw new TypeConversionException("Duration must be strictly positive");
            }
            return result;
        }
    }

    private static final class StringToPositiveInt implements ITypeConverter<Integer> {

        @Override
        public Integer convert(final String value) throws Exception {
            final int result = Integer.parseInt(value);
            if (result <= 0) {
                throw new TypeConversionException("Parallelism must be strictly positive");
            }
            return result;
        }
    }
}
