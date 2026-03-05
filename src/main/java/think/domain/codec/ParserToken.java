package think.domain.codec;

import java.util.regex.Pattern;
import think.domain.codec.Parser.BadMapCodeException;

record ParserToken(int numSkips, Kind kind, int order) {
    enum Kind {
        WALL,
        START,
        FINISH,
        CHECKPOINT,
    }

    private static final Pattern FEATURE_DELIM_RE = Pattern.compile(",");
    private static final Pattern FEATURE_RE = Pattern.compile("[a-z]\\d+");
    private static final int EXPECTED_NUM_PARTS = 2;
    private static final int FEATURE_KIND_SIZE = 1;
    private static final int DEFAULT_SKIP = 0;

    static ParserToken parse(final String rawToken) throws BadMapCodeException {
        final String[] tokenParts = FEATURE_DELIM_RE.split(rawToken, -1);
        ParserSafety.require(tokenParts.length == EXPECTED_NUM_PARTS);

        final int skips = tokenParts[0].isBlank()
            ? DEFAULT_SKIP
            : ParserSafety.stringToInt(tokenParts[0]);
        final String featureString = tokenParts[1];
        ParserSafety.require(FEATURE_RE.matcher(featureString).matches());
        ParserSafety.require(featureString.length() >= FEATURE_KIND_SIZE + 1);

        final String kindString = featureString.substring(0, FEATURE_KIND_SIZE);
        final int order = ParserSafety.stringToInt(featureString.substring(FEATURE_KIND_SIZE));

        return new ParserToken(skips, determineKind(kindString), order);
    }

    private static Kind determineKind(final String kindSymbol) throws BadMapCodeException {
        return switch (kindSymbol) {
            case "r" -> Kind.WALL;
            case "s" -> Kind.START;
            case "f" -> Kind.FINISH;
            case "c" -> Kind.CHECKPOINT;
            default -> throw new BadMapCodeException();
        };
    }
}
