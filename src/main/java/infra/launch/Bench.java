package infra.launch;

import infra.bench.Agreement;
import infra.bench.Latency;
import infra.bench.Optimality;
import infra.bench.Params;
import infra.bench.Score;
import infra.bench.Throughput;
import infra.bench.Timeline;
import java.nio.file.Files;
import java.nio.file.Path;
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
        paramLabel = "<solverKind>",
        description = "Name of solver to use, one of ${COMPLETION-CANDIDATES}"
    )
    private SolverKind solverKind;

    @Parameters(
        paramLabel = "<mapCodeFile>",
        description = "Path to the Pathery MapCode file.",
        converter = StringToPuzzle.class
    )
    private Puzzle mapCodeFile;

    @Parameters(
        paramLabel = "<durationMs>",
        description = "How long to run benchmark for.",
        converter = StringToPositiveLong.class
    )
    private Long durationMs;

    @Parameters(
        paramLabel = "<parallelism>",
        description = "How many identical workers to run.",
        converter = StringToPositiveInt.class
    )
    private Integer parallelism;

    private Bench() {}

    @Override
    public void run() {
        final Params params = new Params(
            Objects.requireNonNull(solverKind),
            Objects.requireNonNull(mapCodeFile),
            Objects.requireNonNull(durationMs),
            Objects.requireNonNull(parallelism)
        );
        switch (Objects.requireNonNull(benchKind)) {
            case AGREEMENT -> new Agreement(params).run();
            case LATENCY -> new Latency(params).run();
            case OPTIMALITY -> new Optimality(params).run();
            case SCORE -> new Score(params).run();
            case THROUGHPUT -> new Throughput(params).run();
            case TIMELINE -> new Timeline(params).run();
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
