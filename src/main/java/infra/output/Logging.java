package infra.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.StackWalker.StackFrame;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

/**
    Centralized logging. It is a design error to print directly instead of using this class. Logging
    messages are tagged with the type of log and the declaring class like so:

    <pre>
    [ANNOUNCEMENT] [infra.launch.App]: Launch point: Application
    [INFO] [think.solvers.Solver]: Started BaselineSolver
    </pre>

    Everything goes to stderr, except for results, which go to stdout. Everything is also saved to
    a log file so you can grep it later. Notice the lack of public error logging. All other classes
    are considered critical and if they experience an error, the system should crash. However,
    logging is not critical, so is allowed to suffer errors and only partially fail.
 */
public final class Logging {

    private static final String LOG_FILE = "mayulime.log";
    private static final BufferedWriter WRITER;
    private static final boolean ENABLED = true;
    private static final Locale LOCALE = Locale.ENGLISH;
    private static final StackWalker WALKER = StackWalker.getInstance(
        StackWalker.Option.RETAIN_CLASS_REFERENCE
    );

    static {
        BufferedWriter initializedWriter;
        try {
            initializedWriter = Files.newBufferedWriter(
                Path.of(LOG_FILE),
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE
            );
        } catch (final IOException exception) {
            exception.printStackTrace();
            initializedWriter = null;
        }
        WRITER = initializedWriter;
    }

    private Logging() {}

    public static void results(final String message, final Object... args) {
        log(System.out, "RESULTS", message, args);
    }

    public static void announcement(final String message, final Object... args) {
        log(System.err, "ANNOUNCEMENT", message, args);
    }

    public static void warning(final String message, final Object... args) {
        log(System.err, "WARNING", message, args);
    }

    public static void info(final String message, final Object... args) {
        log(System.err, "INFO", message, args);
    }

    public static void debug(final String message, final Object... args) {
        log(System.err, "DEBUG", message, args);
    }

    private static synchronized void log(
        final PrintStream printStream,
        final String category,
        final String message,
        final Object... args
    ) {
        if (!ENABLED) {
            return;
        }

        final Function<Stream<StackFrame>, String> getCaller = frames ->
            frames
                .filter(frame -> frame.getDeclaringClass() != Logging.class)
                .map(frame -> frame.getDeclaringClass().getName())
                .findFirst()
                .orElse("<unknown>");
        final String caller = WALKER.walk(getCaller);

        String formatted = "";
        try {
            formatted = String.format(message, args);
        } catch (IllegalFormatException exception) {
            exception.printStackTrace();
            return;
        }

        final String line = String.format(LOCALE, "[%s] [%s]: %s", category, caller, formatted);
        printStream.println(line);
        if (WRITER != null) {
            try {
                WRITER.write(line);
                WRITER.newLine();
                WRITER.flush();
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
