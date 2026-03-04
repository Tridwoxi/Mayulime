package domain.codec;

import domain.codec.Parser.BadMapCodeException;
import java.util.regex.Pattern;

/**
    String and numeric manipulation for safe parsing of MapCodes. This class defends against
    negative numbers and resource exhaustion attacks.
 */
final class ParserSafety {

    private static final Pattern DIGITS_RE = Pattern.compile("\\d+");
    private static final Pattern LINEBREAK_RE = Pattern.compile("\\R");
    private static final int MAX_NAME_LENGTH = 100;
    private static final int BIG_NUMBER = 20000; // Mazes may be no more than 100x100 and some more.

    private ParserSafety() {}

    static void require(final boolean condition) throws BadMapCodeException {
        if (!condition) {
            throw new BadMapCodeException();
        }
    }

    static int stringToInt(final String string) throws BadMapCodeException {
        try {
            require(DIGITS_RE.matcher(string).matches());
            final int value = Integer.parseInt(string);
            require(value >= 0 && value <= BIG_NUMBER);
            return value;
        } catch (NumberFormatException exception) {
            throw new BadMapCodeException();
        }
    }

    static int sum(final int first, final int second) throws BadMapCodeException {
        try {
            final int value = Math.addExact(first, second);
            require(value <= BIG_NUMBER);
            return value;
        } catch (ArithmeticException exception) {
            throw new BadMapCodeException();
        }
    }

    static int multiply(final int first, final int second) throws BadMapCodeException {
        try {
            final int value = Math.multiplyExact(first, second);
            require(value > 0 && value < BIG_NUMBER);
            return value;
        } catch (ArithmeticException exception) {
            throw new BadMapCodeException();
        }
    }

    static String cleanName(final String string) {
        final String onOneLine = LINEBREAK_RE.matcher(string.strip()).replaceAll(" ");
        if (onOneLine.length() > MAX_NAME_LENGTH) {
            return onOneLine.substring(0, MAX_NAME_LENGTH) + "...";
        }
        return onOneLine;
    }
}
