package infra.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.StackWalker.StackFrame;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

/**
    Centralized logging. It is a design error to print directly instead of using this class. Log
    messages are always saved to a file. Beware, the log file may be large. Log messages are tagged
    with the type of log and the declaring class like so:

    <pre>
    [ANNOUNCEMENT] [infra.launch.App]: Launch point: Application
    [INFO] [think.solvers.Solver]: Started BaselineSolver
    </pre>

    Results are always printed to stdout. If {@link #NOISY_OUTPUT_ENVVAR} is set to "true", all log
    messages are printed to stderr as well. Notice the lack of public error logging. All other
    classes are considered critical and if they experience an error, the system should crash.
    However, logging is not critical, so is allowed to suffer errors and only partially fail.
 */
public final class Logger {

    public static final String NOISY_OUTPUT_ENVVAR = "MAYULIME_NOISY_OUTPUT";
    private static final String LOG_FILE = "mayulime.log";
    private static final BufferedWriter WRITER;
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

    private Logger() {}

    public static void results(final String message, final Object... args) {
        System.out.println(format(message, args));
        log("RESULTS", message, args);
    }

    public static void announcement(final String message, final Object... args) {
        log("ANNOUNCEMENT", message, args);
    }

    public static void warning(final String message, final Object... args) {
        log("WARNING", message, args);
    }

    public static void info(final String message, final Object... args) {
        log("INFO", message, args);
    }

    public static void debug(final String message, final Object... args) {
        log("DEBUG", message, args);
    }

    private static String format(final String message, final Object... args) {
        try {
            return String.format(message, args);
        } catch (IllegalFormatException exception) {
            exception.printStackTrace();
            return "";
        }
    }

    private static synchronized void log(
        final String category,
        final String message,
        final Object... args
    ) {
        final Function<Stream<StackFrame>, String> getCaller = frames ->
            frames
                .filter(frame -> frame.getDeclaringClass() != Logger.class)
                .map(frame -> frame.getDeclaringClass().getName())
                .findFirst()
                .orElse("<unknown>");
        final String caller = WALKER.walk(getCaller);

        final String line = String.format(
            LOCALE,
            "[%s] [%s]: %s",
            category,
            caller,
            format(message, args)
        );

        if (WRITER != null) {
            try {
                WRITER.write(line);
                WRITER.newLine();
                WRITER.flush();
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }

        final String noisyOrNull = System.getenv(NOISY_OUTPUT_ENVVAR);
        if (noisyOrNull != null && noisyOrNull.strip().equalsIgnoreCase("true")) {
            System.err.println(line);
        }
    }
}
