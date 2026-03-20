package infra.launch;

import infra.bench.Score;
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
import think.manager.SolverKind;

/**
    Development-only benchmark runner.
 */
@Command(name = "bench", version = "Mayulime 0.1.0", mixinStandardHelpOptions = true)
public final class Bench implements Runnable {

    public record Params(SolverKind solverKind, Puzzle puzzle, long durationMs) {}

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
        converter = StringToNonNegativeLong.class
    )
    private Long durationMs;

    private Bench() {}

    @Override
    public void run() {
        final Params params = new Params(
            Objects.requireNonNull(solverKind),
            Objects.requireNonNull(mapCodeFile),
            Objects.requireNonNull(durationMs)
        );
        switch (Objects.requireNonNull(benchKind)) {
            case SCORE -> new Score().accept(params);
            default -> throw new AssertionError();
        }
    }

    public static void main(final String[] args) {
        System.exit(
            new CommandLine(new Bench()).setCaseInsensitiveEnumValuesAllowed(true).execute(args)
        );
    }

    private enum BenchKind {
        SCORE,
    }

    private static final class StringToPuzzle implements ITypeConverter<Puzzle> {

        @Override
        public Puzzle convert(final String value) throws Exception {
            return Parser.parse(Files.readString(Path.of(value)).strip());
        }
    }

    private static final class StringToNonNegativeLong implements ITypeConverter<Long> {

        @Override
        public Long convert(final String value) throws Exception {
            final long result = Long.parseLong(value);
            if (result < 0L) {
                throw new TypeConversionException("Duration must be non-negative");
            }
            return result;
        }
    }
}
