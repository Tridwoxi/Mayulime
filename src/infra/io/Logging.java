package infra.io;

import java.util.Locale;

/**
    Centralized logging for debugging, progress update, and error messages.
 */
public final class Logging {

    private static final boolean ENABLED = true;
    private static final Locale LOCALE = Locale.ENGLISH;

    private Logging() {}

    /**
        Print the message to stderr. It is a design error to print in any other manner.

        @param
            getClass whatever the method "getClass(void)" returns at the call site, or
            whatever the equivalent is in static contexts
        @param
            message the message to print
        @param
            args arguments to format the message
     */
    public static void log(
        final Class<?> getClass,
        final String message,
        final Object... args
    ) {
        if (ENABLED) {
            final String formatted = String.format(message, args);
            System.err.printf(LOCALE, "[%s]: %s%n", getClass.getName(), formatted);
        }
    }
}
