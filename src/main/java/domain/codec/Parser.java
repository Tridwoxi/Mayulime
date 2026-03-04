package domain.codec;

import domain.model.Maze;
import domain.model.Maze.Feature;
import domain.model.Puzzle;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.regex.Pattern;

/**
    This class constructs a {@link Puzzle} from a string, or throws a {@link BadMapCodeException}
    if it is unable (either because the input is malformed or contains unsupported features).

    Simplified Pathery MapCode (please see https://www.pathery.com/mapeditor) grammar:

    <pre>
    MapCode         -> Metadata : Maze
    Metadata        -> NumCols . NumRows . BlockingBudget . Name . . .
    NumCols         -> int
    NumRows         -> int
    BlockingBudget  -> int
    Name            -> string
    Maze            -> Token Maze | nothing
    Token           -> Skip , Kind Order .
    Skip            -> int | nothing
    Kind            -> s | f | r | c
    Order           -> int
    </pre>

    Integers contain only digits and are strictly positive. {@code |} and {@code nothing} are
    metasymbols representing alternation and an empty production. There are kinds beyond those
    defined here, and can be learned of by looking at the map editor. Name must not contain the
    {@code .} symbol.

    This parser also enforces semantic correctness: features stay within bounds, checkpoints must
    have unique orders, and blocking budget cannot exceed blank cells. Pathery supports variants
    with multiple starts / finishes / checkpoints, but we do not yet. Player walls are not
    supported because they are part of solutions, not problem specifications.
 */
public final class Parser {

    public static final class BadMapCodeException extends Exception {}

    private record MazeData(
        Maze maze,
        int[] checkpoints,
        int numBlankCells,
        EnumSet<Puzzle.Mechanic> mechanics
    ) {}

    private static final Pattern REGION_DELIM_RE = Pattern.compile(":");
    private static final Pattern TOKEN_DELIM_RE = Pattern.compile("\\.");
    private static final int EXPECTED_REGIONS_SIZE = 2;
    private static final int EXPECTED_METADATA_SIZE = 7;

    private Parser() {}

    public static Puzzle parse(final String mapCode) throws BadMapCodeException {
        final String[] regions = REGION_DELIM_RE.split(mapCode.strip(), -1);
        ParserSafety.require(regions.length == EXPECTED_REGIONS_SIZE);

        final String[] metadata = TOKEN_DELIM_RE.split(regions[0], -1);
        ParserSafety.require(metadata.length == EXPECTED_METADATA_SIZE);

        final int numCols = ParserSafety.stringToInt(metadata[0]);
        final int numRows = ParserSafety.stringToInt(metadata[1]);
        final int blockingBudget = ParserSafety.stringToInt(metadata[2]);
        final String puzzleName = ParserSafety.cleanName(metadata[3]);
        ParserSafety.multiply(numRows, numCols);

        final MazeData mazeData = parseMaze(regions[1], numRows, numCols);
        final int clampedBudget = Math.min(blockingBudget, mazeData.numBlankCells());

        return new Puzzle(
            puzzleName,
            mazeData.maze(),
            mazeData.checkpoints(),
            clampedBudget,
            mazeData.mechanics()
        );
    }

    @SuppressWarnings({ "checkstyle:CyclomaticComplexity", "checkstyle:ExecutableStatementCount" })
    private static MazeData parseMaze(final String rawMaze, final int numRows, final int numCols)
        throws BadMapCodeException {
        final int numCells = ParserSafety.multiply(numRows, numCols);
        final String[] tokens = TOKEN_DELIM_RE.split(rawMaze, -1);

        final Feature[] grid = new Feature[numCells];
        Arrays.fill(grid, Feature.BLANK);
        final ParserCheckpoints checkpoints = new ParserCheckpoints();
        final EnumSet<Puzzle.Mechanic> mechanics = EnumSet.noneOf(Puzzle.Mechanic.class);

        int traversingIndex = 0;
        for (int index = 0; index < tokens.length - 1; index += 1) {
            final ParserToken token = ParserToken.parse(tokens[index]);
            final int featureIndex = ParserSafety.sum(traversingIndex, token.numSkips());
            ParserSafety.require(featureIndex < numCells);

            switch (token.kind()) {
                case WALL -> {
                    if (token.order() == 1 || token.order() == 3) {
                        grid[featureIndex] = Feature.SYSTEM_WALL;
                    } else {
                        throw new BadMapCodeException();
                    }
                }
                case START -> {
                    checkpoints.observeStart(featureIndex, token.order());
                    grid[featureIndex] = Feature.CHECKPOINT;
                }
                case FINISH -> {
                    checkpoints.observeFinish(featureIndex, token.order());
                    grid[featureIndex] = Feature.CHECKPOINT;
                }
                case CHECKPOINT -> {
                    checkpoints.observeCheckpoint(featureIndex, token.order());
                    grid[featureIndex] = Feature.CHECKPOINT;
                }
                default -> throw new AssertionError();
            }
            traversingIndex = ParserSafety.sum(featureIndex, 1);
        }

        final int[] orderedCheckpoints = checkpoints.toOrderedArray();

        int numBlankCells = 0;
        for (final Feature feature : grid) {
            if (feature == Feature.BLANK) {
                numBlankCells += 1;
            }
        }

        return new MazeData(
            new Maze(grid, numRows, numCols),
            orderedCheckpoints,
            numBlankCells,
            mechanics
        );
    }
}
