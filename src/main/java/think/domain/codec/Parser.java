package think.domain.codec;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import think.domain.model.Feature;
import think.domain.model.Puzzle;

/**
    Construct a {@link Puzzle} from a string, or throw a {@link BadMapCodeException} if we are
    unable (either because the input is malformed or contains unsupported features).
 */
public final class Parser {

    public static final class BadMapCodeException extends Exception {}

    public static final String UNNAMED_PUZZLE_NAME = "Unnamed Puzzle";
    private static final Pattern REGION_DELIM_RE = Pattern.compile(":");
    private static final Pattern TOKEN_DELIM_RE = Pattern.compile("\\.");
    private static final int EXPECTED_REGIONS_SIZE = 2;
    private static final int EXPECTED_METADATA_SIZE = 7;
    private static final List<Integer> SYSTEM_WALL_ORDERS = List.of(1, 3);
    private static final List<Integer> PLAYER_WALL_ORDERS = List.of(2);

    private Parser() {}

    public static Puzzle parse(final String mapCode) throws BadMapCodeException {
        final String[] regions = REGION_DELIM_RE.split(mapCode.strip(), -1);
        ParserSafety.require(regions.length == EXPECTED_REGIONS_SIZE);

        final String[] metadata = TOKEN_DELIM_RE.split(regions[0], -1);
        ParserSafety.require(metadata.length == EXPECTED_METADATA_SIZE);

        final int numCols = ParserSafety.stringToInt(metadata[0]);
        final int numRows = ParserSafety.stringToInt(metadata[1]);
        final int blockingBudget = ParserSafety.stringToNonNegativeInt(metadata[2]);
        final String cleanedName = ParserSafety.cleanName(metadata[3]);
        final String puzzleName = cleanedName.isBlank() ? UNNAMED_PUZZLE_NAME : cleanedName;
        ParserSafety.multiply(numRows, numCols);

        final MazeData mazeData = parseMaze(regions[1], numRows, numCols);
        final int remainingBudget = blockingBudget - mazeData.consumedBudget();
        final int clampedBudget = Math.min(remainingBudget, mazeData.numBlankCells());
        ParserSafety.require(clampedBudget >= 0);
        return new Puzzle(
            puzzleName,
            mazeData.features(),
            numRows,
            numCols,
            mazeData.checkpoints(),
            clampedBudget
        );
    }

    private static MazeData parseMaze(final String rawMaze, final int numRows, final int numCols)
        throws BadMapCodeException {
        final int numCells = ParserSafety.multiply(numRows, numCols);
        final String[] tokens = TOKEN_DELIM_RE.split(rawMaze, -1);

        final Feature[] maze = new Feature[numCells];
        Arrays.fill(maze, Feature.BLANK);
        final ParserCheckpoints checkpoints = new ParserCheckpoints();

        int consumedBudget = 0;
        int traversingIndex = 0;
        for (int index = 0; index < tokens.length - 1; index += 1) {
            final ParserToken token = ParserToken.parse(tokens[index]);
            final int featureIndex = ParserSafety.sum(traversingIndex, token.numSkips());
            ParserSafety.require(featureIndex < numCells);

            switch (token.kind()) {
                case WALL -> {
                    if (SYSTEM_WALL_ORDERS.contains(token.order())) {
                        maze[featureIndex] = Feature.SYSTEM_WALL;
                    } else if (PLAYER_WALL_ORDERS.contains(token.order())) {
                        maze[featureIndex] = Feature.SYSTEM_WALL;
                        consumedBudget += 1;
                    } else {
                        throw new BadMapCodeException();
                    }
                }
                case START -> {
                    checkpoints.observeStart(featureIndex, token.order());
                    maze[featureIndex] = Feature.CHECKPOINT;
                }
                case FINISH -> {
                    checkpoints.observeFinish(featureIndex, token.order());
                    maze[featureIndex] = Feature.CHECKPOINT;
                }
                case CHECKPOINT -> {
                    checkpoints.observeCheckpoint(featureIndex, token.order());
                    maze[featureIndex] = Feature.CHECKPOINT;
                }
                default -> throw new AssertionError();
            }
            traversingIndex = ParserSafety.sum(featureIndex, 1);
        }

        final int[] orderedCheckpoints = checkpoints.toOrderedArray();

        int numBlankCells = 0;
        for (final Feature feature : maze) {
            if (feature == Feature.BLANK) {
                numBlankCells += 1;
            }
        }
        return new MazeData(maze, orderedCheckpoints, numBlankCells, consumedBudget);
    }

    private record MazeData(
        Feature[] features,
        int[] checkpoints,
        int numBlankCells,
        int consumedBudget
    ) {}
}
