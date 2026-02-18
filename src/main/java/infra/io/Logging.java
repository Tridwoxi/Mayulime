package infra.io;

import java.io.PrintStream;
import java.lang.StackWalker.StackFrame;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

/**
    Centralized logging. It is a design error to print directly instead of using this class. The
    logging messages always use the declaring class, rather than the actual class at runtime, so
    pass that too if it's useful). Notice how there is no error logging: if the system encounters
    an error, it should simply crash.
 */
public final class Logging {

    private static final boolean ENABLED = true;
    private static final Locale LOCALE = Locale.ENGLISH;
    private static final StackWalker WALKER = StackWalker.getInstance(
        StackWalker.Option.RETAIN_CLASS_REFERENCE
    );

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

    private static void log(
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
        final String formatted = String.format(message, args);
        printStream.printf(LOCALE, "[%s] [%s]: %s%n", category, caller, formatted);
    }
}
