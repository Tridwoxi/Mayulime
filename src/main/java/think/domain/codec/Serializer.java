package think.domain.codec;

import java.util.Optional;
import think.domain.model.Feature;
import think.domain.model.Puzzle;

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
        return serialize(puzzle, puzzle.getFeatures());
    }

    public static String serialize(final Puzzle puzzle, final Feature[] features) {
        if (!puzzle.isValid(features)) {
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
        appendMaze(mapCode, puzzle, features);
        return mapCode.toString();
    }

    private static void appendMaze(
        final StringBuilder mapCode,
        final Puzzle puzzle,
        final Feature[] features
    ) {
        final int[] checkpoints = puzzle.getCheckpoints();
        int traversingIndex = 0;

        for (int index = 0; index < features.length; index += 1) {
            final Optional<String> token = tokenAt(index, checkpoints, features[index]);
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
        final int[] checkpoints,
        final Feature feature
    ) {
        return switch (feature) {
            case BLANK -> Optional.empty();
            case SYSTEM_WALL -> Optional.of("r1");
            case PLAYER_WALL -> Optional.of("r2");
            case CHECKPOINT -> Optional.of(checkpointToken(index, checkpoints));
        };
    }

    private static String checkpointToken(final int index, final int[] checkpoints) {
        for (int order = 0; order < checkpoints.length; order += 1) {
            if (checkpoints[order] != index) {
                continue;
            }
            if (order == 0) {
                return "s" + START_ORDER;
            }
            if (order == checkpoints.length - 1) {
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
