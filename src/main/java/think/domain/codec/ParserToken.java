package think.domain.codec;

import java.util.regex.Pattern;
import think.domain.codec.Parser.BadMapCodeException;

record ParserToken(int numSkips, Kind kind, int order) {
    enum Kind {
        WALL,
        START,
        FINISH,
        WAYPOINT,
    }

    private static final Pattern TILE_DELIM_RE = Pattern.compile(",");
    private static final Pattern TILE_RE = Pattern.compile("[a-z]\\d+");
    private static final int EXPECTED_NUM_PARTS = 2;
    private static final int TILE_KIND_SIZE = 1;
    private static final int DEFAULT_SKIP = 0;

    static ParserToken parse(final String rawToken) throws BadMapCodeException {
        final String[] tokenParts = TILE_DELIM_RE.split(rawToken, -1);
        ParserSafety.require(tokenParts.length == EXPECTED_NUM_PARTS);

        final int skips = tokenParts[0].isBlank()
            ? DEFAULT_SKIP
            : ParserSafety.stringToInt(tokenParts[0]);
        final String tileString = tokenParts[1];
        ParserSafety.require(TILE_RE.matcher(tileString).matches());
        ParserSafety.require(tileString.length() >= TILE_KIND_SIZE + 1);

        final String kindString = tileString.substring(0, TILE_KIND_SIZE);
        final int order = ParserSafety.stringToInt(tileString.substring(TILE_KIND_SIZE));

        return new ParserToken(skips, determineKind(kindString), order);
    }

    private static Kind determineKind(final String kindSymbol) throws BadMapCodeException {
        return switch (kindSymbol) {
            case "r" -> Kind.WALL;
            case "s" -> Kind.START;
            case "f" -> Kind.FINISH;
            case "c" -> Kind.WAYPOINT;
            default -> throw new BadMapCodeException();
        };
    }
}
