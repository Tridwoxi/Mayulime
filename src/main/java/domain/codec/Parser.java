package domain.codec;

import domain.model.Puzzle;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import solvers.graph.impl.GridGraph;
import solvers.graph.impl.GridGraph.Cell;

/**
    Convert a Pathery MapCode to a puzzle.
 */
/*
    A Pathery MapCode consists of metadata and board regions, separated by a colon.

    The metadata region has 7 period-delimited sections, which are width, height, wall budget,
    name, and then some mystery things I don't understand.

    The board region has any number of period-delimited tokens. Each token is a number of skips
    (optional int, default 0) and feature, seperated by a comma. The feature takes the form of a
    kind (string) and an order (integer).

    Supported features:
    - s1: Green start
    - f1: Finish
    - r1, r3: System wall
    - cN: Checkpoint

    Unsupported features:
    - tN: Teleport-in
    - uN: Teleport-out
    - s2: Red start
    - p1: Unbuildable
    - x1: Green single path
    - x2: Red single path
    - z5: Ice
    - r2: Player wall (since these are from solutions, rather than problems).
 */
public final class Parser {

    public static final class BadMapCodeException extends Exception {}

    private static final Pattern REGION_DELIM_RE = Pattern.compile(":");
    private static final Pattern TOKEN_DELIM_RE = Pattern.compile("\\.");
    private static final int EXPECTED_REGIONS_SIZE = 2;
    private static final int EXPECTED_METADATA_SIZE = 7;

    private Parser() {}

    @SuppressWarnings({ "checkstyle:CyclomaticComplexity", "checkstyle:ExecutableStatementCount" })
    public static Puzzle parse(final String mapCode) throws BadMapCodeException {
        final String[] regions = REGION_DELIM_RE.split(mapCode.strip());
        Safety.require(regions.length == EXPECTED_REGIONS_SIZE);

        final String[] metadata = TOKEN_DELIM_RE.split(regions[0], -1);
        Safety.require(metadata.length == EXPECTED_METADATA_SIZE);
        final int numCols = Safety.stringToInt(metadata[0]);
        final int numRows = Safety.stringToInt(metadata[1]);
        final int wallBudget = Safety.stringToInt(metadata[2]);
        final String puzzleName = Safety.cleanName(metadata[3]);
        final int numCells = Safety.multiply(numRows, numCols);

        final String[] boarddata = TOKEN_DELIM_RE.split(regions[1], -1);
        final Checkpoints checkpoints = new Checkpoints();
        final GridGraph<Void> grid = new GridGraph<>(numRows, numCols, ignoredCell ->
            Optional.empty()
        );
        final List<Cell> allCells = List.copyOf(grid.getAllPossibleCells());
        if (allCells.size() != numCells) {
            throw new IllegalStateException();
        }
        final Set<Cell> originallyEmpty = new HashSet<>(allCells);
        final Set<Cell> originallyMissing = new HashSet<>();
        final Set<Cell> originallyCheckpoint = new HashSet<>();

        int traversingIndex = 0;
        // The token delimiter is not quite a delimiter, but rather an ender. Hence length-1.
        for (int index = 0; index < boarddata.length - 1; index++) {
            final Token token = Token.parse(boarddata[index]);

            final int featureIndex = Safety.sum(traversingIndex, token.skips());
            Safety.require(featureIndex < numCells);
            final Cell featureCell = allCells.get(featureIndex);

            switch (token.kind()) {
                case WALL -> {
                    Safety.require(token.order() == 1 || token.order() == 3);
                    originallyEmpty.remove(featureCell);
                    originallyCheckpoint.remove(featureCell);
                    originallyMissing.add(featureCell);
                }
                case START -> {
                    Safety.require(token.order() == 1);
                    checkpoints.setStart(featureCell);
                    originallyEmpty.remove(featureCell);
                    originallyMissing.remove(featureCell);
                    originallyCheckpoint.add(featureCell);
                }
                case FINISH -> {
                    Safety.require(token.order() == 1);
                    checkpoints.setFinish(featureCell);
                    originallyEmpty.remove(featureCell);
                    originallyMissing.remove(featureCell);
                    originallyCheckpoint.add(featureCell);
                }
                case CHECKPOINT -> {
                    Safety.require(token.order() >= 1);
                    checkpoints.addCheckpoint(featureCell, token.order());
                    originallyEmpty.remove(featureCell);
                    originallyMissing.remove(featureCell);
                    originallyCheckpoint.add(featureCell);
                }
                default -> throw new AssertionError();
            }
            traversingIndex = Safety.sum(featureIndex, 1);
        }

        final int numEmptyCells = originallyEmpty.size();
        final int clampedBlockingBudget = Math.min(wallBudget, numEmptyCells);

        return new Puzzle(
            puzzleName,
            numRows,
            numCols,
            originallyEmpty,
            originallyMissing,
            originallyCheckpoint,
            checkpoints.toOrderedPath(),
            clampedBlockingBudget
        );
    }
}
