package infra.launch;

import infra.launch.Parameters.InvalidArgumentsException;
import infra.output.Logging;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import think.domain.codec.Parser;
import think.domain.codec.Parser.BadMapCodeException;
import think.domain.codec.Serializer;
import think.domain.model.Puzzle;
import think.manager.Manager;
import think.manager.Manager.Proposal;
import think.manager.SolverRegistry;
import think.manager.SolverRegistry.NoSuchSolverException;

/**
    Development-only headless launch point. Runs the solver specified by its first command line
    argument against Huge1 (a particularly difficult puzzle) as a benchmark.
 */
public final class Bench {

    private final Parameters params;

    private Bench(final Parameters params) {
        this.params = params;
    }

    private void run() {
        final BlockingQueue<Proposal> queue = new LinkedBlockingQueue<>();
        final Manager manager = new Manager(queue::add, List.of(params.registry()));

        manager.solve(params.puzzle());
        try {
            Thread.sleep(params.timeoutMs());
        } catch (InterruptedException exception) {
            Logging.warning("%s", exception.toString());
        }
        manager.stop();

        final List<Proposal> results = new ArrayList<>(queue.size());
        queue.drainTo(results);
        final Consumer<Proposal> displayResults = best -> {
            Logging.results("Solution: %s", Serializer.serialize(params.puzzle(), best.features()));
            Logging.results("Score: %d", best.score());
            Logging.results("Found after: %d ms", System.currentTimeMillis() - best.createdAtMs());
        };
        final Runnable complain = () -> {
            Logging.results("Nothing found.");
        };
        results
            .stream()
            .max(Comparator.comparingInt(Proposal::score).thenComparingLong(Proposal::createdAtMs))
            .ifPresentOrElse(displayResults, complain);
    }

    public static void main(final String[] args) {
        Logging.announcement("Launch point: Bench");
        try {
            new Bench(Parameters.parseArguments(args)).run();
        } catch (final InvalidArgumentsException ignored) {
            Parameters.printUsage();
            System.exit(1);
        }
        System.exit(0);
    }
}

record Parameters(Puzzle puzzle, SolverRegistry registry, long timeoutMs) {
    static final class InvalidArgumentsException extends Exception {}

    static Parameters parseArguments(final String[] args) throws InvalidArgumentsException {
        if (args.length != 3) {
            throw new InvalidArgumentsException();
        }

        final Puzzle puzzle;
        try {
            puzzle = Parser.parse(Files.readString(Path.of(args[0])));
        } catch (
            InvalidPathException
            | IOException
            | OutOfMemoryError
            | BadMapCodeException ignored
        ) {
            throw new InvalidArgumentsException();
        }

        final SolverRegistry registry;
        try {
            registry = SolverRegistry.fromString(args[1]);
        } catch (final NoSuchSolverException ignored) {
            throw new InvalidArgumentsException();
        }

        final long timeoutMs;
        try {
            timeoutMs = Long.parseLong(args[2]);
            if (timeoutMs < 0) {
                throw new InvalidArgumentsException();
            }
        } catch (final NumberFormatException exception) {
            throw new InvalidArgumentsException();
        }

        return new Parameters(puzzle, registry, timeoutMs);
    }

    static void printUsage() {
        Logging.warning("Usage: gradle bench --args=\"MAPCODE_PATH SOLVER_NAME TIMEOUT_MS\"");
        Logging.warning("MAPCODE_PATH (path): path to the MapCode file to solve");
        Logging.warning("SOLVER_NAME (text): one of [%s]", SolverRegistry.prettyNameAll());
        Logging.warning("TIMEOUT_MS (number): how many miliseconds to run the solver for");
    }
}
