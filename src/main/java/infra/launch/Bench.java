package infra.launch;

import infra.launch.Parameters.UnparseableArgumentsException;
import infra.output.Logging;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import think.domain.codec.Parser;
import think.domain.codec.Parser.BadMapCodeException;
import think.domain.codec.Serializer;
import think.domain.model.Puzzle;
import think.manager.Manager;
import think.manager.Manager.Proposal;
import think.manager.SolverKind;
import think.manager.SolverKind.NoSuchSolverException;

/**
    Development-only headless launch point.
 */
public final class Bench {

    private final Parameters params;
    private Proposal best;

    private Bench(final Parameters params) {
        this.params = params;
        this.best = null;
    }

    private void run() {
        final Consumer<Proposal> listener = statusUpdate -> {
            if (best == null || statusUpdate.score() > best.score()) {
                best = statusUpdate;
            }
        };

        final long startTimeMs;
        try (Manager manager = new Manager(listener, List.of(params.solverKind()))) {
            startTimeMs = System.currentTimeMillis();
            manager.solve(params.puzzle());
            try {
                Thread.sleep(params.timeoutMs());
            } catch (InterruptedException exception) {
                Logging.warning("%s", exception.toString());
            }
            manager.stop();
        }

        final Consumer<Proposal> displayResults = best -> {
            Logging.results("Solution: %s", Serializer.serialize(params.puzzle(), best.features()));
            Logging.results("Score: %d", best.score());
            Logging.results("Found after: %d ms", best.createdAtMs() - startTimeMs);
        };
        final Runnable complain = () -> {
            Logging.results("Nothing found.");
        };
        Optional.ofNullable(best).ifPresentOrElse(displayResults, complain);
    }

    public static void main(final String[] args) {
        Logging.announcement("Launch point: Bench");
        try {
            new Bench(Parameters.parseArguments(args)).run();
        } catch (UnparseableArgumentsException _) {
            Parameters.printUsage();
            System.exit(1);
        }
        System.exit(0);
    }
}

record Parameters(Puzzle puzzle, SolverKind solverKind, long timeoutMs) {
    static final class UnparseableArgumentsException extends Exception {}

    static Parameters parseArguments(final String[] args) throws UnparseableArgumentsException {
        if (args.length != 3) {
            throw new UnparseableArgumentsException();
        }

        final Puzzle puzzle;
        try {
            puzzle = Parser.parse(Files.readString(Path.of(args[0])));
        } catch (InvalidPathException | IOException | OutOfMemoryError | BadMapCodeException _) {
            throw new UnparseableArgumentsException();
        }

        final SolverKind solverKind;
        try {
            solverKind = SolverKind.parse(args[1]);
        } catch (NoSuchSolverException _) {
            throw new UnparseableArgumentsException();
        }

        final long timeoutMs;
        try {
            timeoutMs = Long.parseLong(args[2]);
            if (timeoutMs < 0) {
                throw new UnparseableArgumentsException();
            }
        } catch (NumberFormatException _) {
            throw new UnparseableArgumentsException();
        }

        return new Parameters(puzzle, solverKind, timeoutMs);
    }

    static void printUsage() {
        Logging.warning("Usage: gradle bench --args=\"MAPCODE_PATH SOLVER_NAME TIMEOUT_MS\"");
        Logging.warning("MAPCODE_PATH (path): path to the MapCode file to solve");
        Logging.warning(
            "SOLVER_NAME (text): one of [%s]",
            String.join("|", Arrays.stream(SolverKind.values()).map(Enum::toString).toList())
        );
        Logging.warning("TIMEOUT_MS (number): how many miliseconds to run the solver for");
    }
}
