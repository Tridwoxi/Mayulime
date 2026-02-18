package infra.main;

import infra.io.Logging;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import think.Manager;
import think.repr.Grid;
import think.repr.Problem;
import think.repr.Problem.BadMapCodeException;
import think.repr.Problem.Feature;

/**
    Development-only headless alternative program launch point.

    Subclasses of javafx.application.Application, such as infra.main.App, have bad ideas about
    program lifecycle. This class does the same thing as infra.main.App, except:

    <ul>
        <li>It does not start the JavaFX runtime, so produces no GUI</li>
        <li>It takes an argument for the map file to load</li>
        <li>It exits after a given amount of time (also given as argument)</li>
    </ul>
 */
public final class Headless {

    private static final int SUCCESS = 0;
    private static final int FAILURE = 1;

    public static void main(final String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            exception.printStackTrace();
            System.exit(FAILURE);
        });
        System.exit(new Headless().run(args));
    }

    private int run(final String[] args) {
        Logging.announcement("Launch point: Headless");

        final Config config;
        final Problem problem;
        try {
            config = parseConfig(args);
            problem = new Problem(Files.readString(config.mapCodePath()));
        } catch (IllegalArgumentException ignored) {
            Logging.warning("Failure: bad arguments.");
            return FAILURE;
        } catch (BadMapCodeException ignored) {
            Logging.warning("Failure: bad map code.");
            return FAILURE;
        } catch (IOException ignored) {
            Logging.warning("Failure: can't read file.");
            return FAILURE;
        }
        new Manager(this::recieveSolutionStub).solve(problem);
        try {
            Thread.sleep(1000 * config.timeoutSeconds());
        } catch (InterruptedException exception) {
            throw new AssertionError();
        }
        Logging.announcement("Success: timed out without dying");
        return SUCCESS;
    }

    private void recieveSolutionStub(
        final String submitter,
        final Problem problem,
        final Grid<Feature> solution,
        final int score
    ) {
        // Normally, we would display solutions, but we have no GUI. We also don't know how to
        // turn a problem or solution back into a MapCode. As for logging, the rest of the system
        // handles it. Hence, there's nothing to do here.
    }

    private Config parseConfig(final String[] args) throws IllegalArgumentException {
        if (args.length != 2) {
            throw new IllegalArgumentException();
        }
        final Path mapCodePath = Path.of(args[0]);
        final int timeoutSeconds;
        try {
            timeoutSeconds = Integer.parseInt(args[1]);
            if (timeoutSeconds < 0) {
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException();
        }
        return new Config(mapCodePath, timeoutSeconds);
    }

    private record Config(Path mapCodePath, int timeoutSeconds) {}
}
