package think2.domain.codec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.IntStream;
import think2.domain.codec.Parser.Requirer;
import think2.domain.repr.Board.Feature;
import think2.domain.repr.Puzzle;
import think2.graph.impl.GridGraph;
import think2.graph.impl.GridGraph.Cell;

/**
    Convert a Pathery MapCode to a puzzle.
 */
/*
    A Pathery MapCode consists of metadata and board regions, separated by a colon.

    The metadata region has 7 period-delimited sections, which are width, height, wall budget,
    name, and then some mystery things I don't understand.

    The board region has any number of period-delimited tokens. Each token is a number of skips
    (optional int, default 0) and feature, seperated by a comma. The feature takes the form of a
    type (character) and an order (integer).

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

    private static final String REGION_DELIM_RE = ":";
    private static final String TOKEN_DELIM_RE = "\\.";
    private static final String FEATURE_DELIM_RE = ",";
    private static final String LINEBREAK_RE = "\\R";
    private static final String DIGITS_RE = "\\d+";
    private static final String FEATURE_RE = "[a-z]\\d+";

    private static final int EXPECTED_REGIONS_SIZE = 2;
    private static final int EXPECTED_METADATA_SIZE = 7;
    private static final int EXPECTED_TOKEN_SIZE = 2;

    private static final String WALL = "r";
    private static final String START = "s";
    private static final String FINISH = "f";
    private static final String CHECKPOINT = "c";

    private static final int FEATURE_TYPE_SIZE = 1;
    private static final String DEFAULT_SKIP = "0";
    private static final int MAX_NAME_LENGTH = 100;

    private Parser() {}

    @SuppressWarnings({ "checkstyle:CyclomaticComplexity", "checkstyle:ExecutableStatementCount" })
    public static Puzzle parse(final String mapCode) throws BadMapCodeException {
        final String[] regions = mapCode.split(REGION_DELIM_RE);
        require(regions.length == EXPECTED_REGIONS_SIZE);

        final String[] metadata = regions[0].split(TOKEN_DELIM_RE);
        require(metadata.length == EXPECTED_METADATA_SIZE);
        final int numCols = stringToInt(metadata[0]);
        final int numRows = stringToInt(metadata[1]);
        final int blockingBudget = stringToInt(metadata[2]);
        final String puzzleName = cleanName(metadata[3]);

        final String[] boarddata = regions[1].split(TOKEN_DELIM_RE);
        final CheckpointTracker<BadMapCodeException> checkpointTracker = new CheckpointTracker<>(
            Parser::require
        );
        final GridTracker<String, BadMapCodeException> gridTracker = new GridTracker<>(
            Parser::require,
            numRows,
            numCols
        );

        int traversingIndex = 0;
        // The token delimiter is not quite a delimiter, but rather an ender. Hence length-1.
        for (int index = 0; index < boarddata.length - 1; index++) {
            final String[] tokens = boarddata[index].split(FEATURE_DELIM_RE);
            require(tokens.length == EXPECTED_TOKEN_SIZE);
            final int numSkips = stringToInt(tokens[0].isBlank() ? DEFAULT_SKIP : tokens[0]);
            final String feature = tokens[1];
            require(feature.matches(FEATURE_RE) && feature.length() >= FEATURE_TYPE_SIZE + 1);
            final String featureType = feature.substring(0, FEATURE_TYPE_SIZE);
            final int featureOrder = stringToInt(feature.substring(FEATURE_TYPE_SIZE));

            if (WALL.equals(featureType) && (featureOrder == 1 || featureOrder == 3)) {
                // Do nothing.
            } else if (START.equals(featureType) && featureOrder == 1) {
                checkpointTracker.setStart(traversingIndex);
            } else if (FINISH.equals(featureType) && featureOrder == 1) {
                checkpointTracker.setFinish(traversingIndex);
            } else if (CHECKPOINT.equals(featureType)) {
                checkpointTracker.addCheckpoint(traversingIndex, featureOrder);
            } else {
                require(false);
            }
            gridTracker.add(featureType, numSkips);
            traversingIndex += numSkips + 1;
        }

        final List<Optional<String>> graphBase = gridTracker.get();
        final Function<Cell, Optional<Feature>> graphDataSupplier = cell -> {
            final Function<String, Optional<Feature>> switcher = string ->
                switch (string) {
                    case WALL -> Optional.empty();
                    case START -> Optional.of(Feature.CHECKPOINT);
                    case FINISH -> Optional.of(Feature.CHECKPOINT);
                    case CHECKPOINT -> Optional.of(Feature.CHECKPOINT);
                    default -> throw new IllegalStateException();
                };
            return graphBase.get(cell.row() * numCols + cell.col()).flatMap(switcher);
        };

        final List<Integer> checkpointBase = checkpointTracker.get();
        final Function<Integer, Cell> indexToCell = index ->
            new Cell(index / numCols, index % numCols);
        final int clampedBlockingBudget = Math.min(
            blockingBudget,
            (int) graphBase.stream().filter(Optional::isEmpty).count()
        );

        return new Puzzle(
            puzzleName,
            new GridGraph<>(numRows, numCols, graphDataSupplier),
            checkpointBase.stream().map(indexToCell).toList(),
            clampedBlockingBudget
        );
    }

    private static String cleanName(final String string) {
        final String onOneLine = string.strip().replaceAll(LINEBREAK_RE, " ");
        if (onOneLine.length() > MAX_NAME_LENGTH) {
            return onOneLine.substring(0, MAX_NAME_LENGTH) + "...";
        }
        return onOneLine;
    }

    private static int stringToInt(final String string) throws BadMapCodeException {
        try {
            require(string.matches(DIGITS_RE));
            return Integer.parseInt(string);
        } catch (NumberFormatException exception) {
            throw new BadMapCodeException();
        }
    }

    private static void require(final boolean condition) throws BadMapCodeException {
        if (!condition) {
            throw new BadMapCodeException();
        }
    }

    @FunctionalInterface
    interface Requirer<E extends Exception> {
        void throwIfNot(boolean condition) throws E;
    }
}

final class CheckpointTracker<E extends Exception> {

    private record OrederedIndex(int index, int order) {}

    private static final int SENTINEL = -1;
    private final Requirer<E> requirer;
    private final SortedSet<OrederedIndex> checkpoints;
    private final Set<Integer> observed;
    private int start;
    private int finish;

    CheckpointTracker(final Requirer<E> throwIfNot) throws E {
        this.requirer = throwIfNot;
        this.checkpoints = new TreeSet<>(Comparator.comparingInt(OrederedIndex::order));
        this.observed = new HashSet<>();
        start = SENTINEL;
        finish = SENTINEL;
    }

    void addCheckpoint(final int index, final int order) throws E {
        requirer.throwIfNot(!observed.contains(order));
        observed.add(order);
        checkpoints.add(new OrederedIndex(index, order));
    }

    void setStart(final Integer start) throws E {
        requirer.throwIfNot(this.start == SENTINEL);
        this.start = start;
    }

    void setFinish(final int finish) throws E {
        requirer.throwIfNot(this.finish == SENTINEL);
        this.finish = finish;
    }

    List<Integer> get() throws E {
        requirer.throwIfNot(start != SENTINEL && finish != SENTINEL);
        final List<Integer> result = new ArrayList<>(checkpoints.size() + 2);
        result.add(start);
        result.addAll(checkpoints.stream().map(OrederedIndex::index).toList());
        result.add(finish);
        return result;
    }
}

final class GridTracker<T, E extends Exception> {

    private static final int MAX_SIZE = 1_000_000; // Prevent resource exhaustion.
    private final Requirer<E> requirer;
    private final int expectedSize;
    private final List<Optional<T>> backing;
    private int currentIndex;

    GridTracker(final Requirer<E> requirer, final int numRows, final int numCols) throws E {
        this.requirer = requirer;
        this.expectedSize = numRows * numCols;
        requirer.throwIfNot(expectedSize > 0 && expectedSize < MAX_SIZE);
        this.backing = new ArrayList<>(numRows * numCols);
        IntStream.range(0, expectedSize).forEach(ignored -> backing.add(Optional.empty()));
        this.currentIndex = 0;
    }

    void add(final T item, final int numSkips) throws E {
        requirer.throwIfNot(currentIndex + numSkips < expectedSize);
        backing.set(currentIndex, Optional.of(item));
        currentIndex += numSkips + 1;
    }

    List<Optional<T>> get() throws E {
        return new ArrayList<>(backing);
    }
}
