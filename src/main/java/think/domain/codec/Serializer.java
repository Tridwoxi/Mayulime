package think.domain.codec;

import java.util.Optional;
import think.domain.model.Puzzle;
import think.domain.model.Tile;

/**
    Converts {@link Puzzle}s and their states into Pathery MapCodes. Little effort is made to
    verify the validity of the input because this class does not interact with externals.
 */
public final class Serializer {

    private static final String MYSTERY_METADATA = "...";
    private static final int START_ORDER = 1;
    private static final int FINISH_ORDER = 1;

    private Serializer() {}

    public static String serialize(final Puzzle puzzle) {
        return serialize(puzzle, puzzle.getTiles());
    }

    public static String serialize(final Puzzle puzzle, final Tile[] state) {
        if (!puzzle.isValid(state)) {
            throw new IllegalArgumentException();
        }

        final StringBuilder mapCode = new StringBuilder();
        mapCode
            .append(puzzle.getNumCols())
            .append('.')
            .append(puzzle.getNumRows())
            .append('.')
            .append(puzzle.getBlockingBudget())
            .append('.')
            .append(sanitizeName(puzzle.getName()))
            .append(MYSTERY_METADATA)
            .append(':');
        appendMaze(mapCode, puzzle, state);
        return mapCode.toString();
    }

    private static void appendMaze(
        final StringBuilder mapCode,
        final Puzzle puzzle,
        final Tile[] state
    ) {
        final int[] waypoints = puzzle.getWaypoints();
        int traversingIndex = 0;

        for (int index = 0; index < state.length; index += 1) {
            final Optional<String> token = tokenAt(index, waypoints, state[index]);
            if (token.isEmpty()) {
                continue;
            }
            final int skips = index - traversingIndex;
            if (skips > 0) {
                mapCode.append(skips);
            }
            mapCode.append(',').append(token.orElseThrow()).append('.');
            traversingIndex = index + 1;
        }
    }

    private static Optional<String> tokenAt(
        final int index,
        final int[] waypoints,
        final Tile tile
    ) {
        return switch (tile) {
            case BLANK -> Optional.empty();
            case SYSTEM_WALL -> Optional.of("r1");
            case PLAYER_WALL -> Optional.of("r2");
            case WAYPOINT -> Optional.of(waypointToken(index, waypoints));
        };
    }

    private static String waypointToken(final int index, final int[] waypoints) {
        for (int order = 0; order < waypoints.length; order += 1) {
            if (waypoints[order] != index) {
                continue;
            }
            if (order == 0) {
                return "s" + START_ORDER;
            }
            if (order == waypoints.length - 1) {
                return "f" + FINISH_ORDER;
            }
            return "c" + order;
        }
        throw new IllegalArgumentException();
    }

    private static String sanitizeName(final String rawName) {
        return rawName.strip().replace('.', ' ').replace(':', ' ');
    }
}
