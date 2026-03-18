package infra.launch;

import infra.launch.Parameters.UnparseableArgumentsException;
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
    Development-only headless launch point.
 */
public final class Bench {

    private static final long GRACE_PERIOD_MS = 50;
    private final Parameters params;

    private Bench(final Parameters params) {
        this.params = params;
    }

    private void run() {
        final BlockingQueue<Proposal> queue = new LinkedBlockingQueue<>();
        final Manager manager = new Manager(queue::add, List.of(params.registry()));
        final long startTimeMs = System.currentTimeMillis();

        manager.solve(params.puzzle());
        try {
            Thread.sleep(params.timeoutMs());
        } catch (InterruptedException exception) {
            Logging.warning("%s", exception.toString());
        }
        manager.stop();
        try {
            // Wait for in-flight proposals to finish. Technically this is the wrong way to go
            // about it (you should make stop(void) wait instead), but it's a good enough
            // heuristic. It takes 10 MS to evaluate gargantuan1, so this is a practical solution.
            Thread.sleep(GRACE_PERIOD_MS);
        } catch (InterruptedException exception) {
            Logging.warning("%s", exception.toString());
        }

        final List<Proposal> results = new ArrayList<>(queue.size());
        queue.drainTo(results);
        final Consumer<Proposal> displayResults = best -> {
            Logging.results("Solution: %s", Serializer.serialize(params.puzzle(), best.features()));
            Logging.results("Score: %d", best.score());
            Logging.results("Found after: %d ms", best.createdAtMs() - startTimeMs);
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
        } catch (UnparseableArgumentsException _) {
            Parameters.printUsage();
            System.exit(1);
        }
        System.exit(0);
    }
}

record Parameters(Puzzle puzzle, SolverRegistry registry, long timeoutMs) {
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

        final SolverRegistry registry;
        try {
            registry = SolverRegistry.fromString(args[1]);
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

        return new Parameters(puzzle, registry, timeoutMs);
    }

    static void printUsage() {
        Logging.warning("Usage: gradle bench --args=\"MAPCODE_PATH SOLVER_NAME TIMEOUT_MS\"");
        Logging.warning("MAPCODE_PATH (path): path to the MapCode file to solve");
        Logging.warning("SOLVER_NAME (text): one of [%s]", SolverRegistry.prettyNameAll());
        Logging.warning("TIMEOUT_MS (number): how many miliseconds to run the solver for");
    }
}
