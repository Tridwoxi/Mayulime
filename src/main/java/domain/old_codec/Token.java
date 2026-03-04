package domain.old_codec;

import domain.old_codec.Parser.BadMapCodeException;
import java.util.regex.Pattern;

/**
    Parsed board token from map-code board region.
 */
record Token(int skips, Kind kind, int order) {
    enum Kind {
        WALL("r"),
        START("s"),
        FINISH("f"),
        CHECKPOINT("c");

        private final String symbol;

        Kind(final String symbol) {
            this.symbol = symbol;
        }

        private static Kind fromSymbol(final String symbol) throws BadMapCodeException {
            for (Kind kind : Kind.values()) {
                if (kind.symbol.equals(symbol)) {
                    return kind;
                }
            }
            throw new BadMapCodeException();
        }
    }

    private static final Pattern FEATURE_DELIM_RE = Pattern.compile(",");
    private static final Pattern FEATURE_RE = Pattern.compile("[a-z]\\d+");
    private static final int EXPECTED_NUM_PARTS = 2;
    private static final int FEATURE_KIND_SIZE = 1;
    private static final int DEFAULT_SKIP = 0;

    static Token parse(final String rawToken) throws BadMapCodeException {
        final String[] tokenParts = FEATURE_DELIM_RE.split(rawToken, -1);
        Safety.require(tokenParts.length == EXPECTED_NUM_PARTS);

        final int skips = tokenParts[0].isBlank()
            ? DEFAULT_SKIP
            : Safety.stringToInt(tokenParts[0]);
        final String feature = tokenParts[1];
        Safety.require(FEATURE_RE.matcher(feature).matches());
        Safety.require(feature.length() >= FEATURE_KIND_SIZE + 1);

        final String kindSymbol = feature.substring(0, FEATURE_KIND_SIZE);
        final int order = Safety.stringToInt(feature.substring(FEATURE_KIND_SIZE));
        return new Token(skips, Kind.fromSymbol(kindSymbol), order);
    }
}
