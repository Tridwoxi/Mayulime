package think.repr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

/**
    Simplified Pathery problem specification.
 */
public final class Problem {

    public static final class InvalidSpecException extends Exception {}

    private static final String SECTION_DELIM = ";;;";
    private static final String ROW_DELIM = ";;";
    private static final String CELL_DELIM = ";";
    private static final String BRICK = "#";
    private static final String EMPTY = ".";

    private final int numRubbers;
    private final Grid isBrick;
    private final long[] checkpoints;

    // == Constructor and parser. ======================================================

    public Problem(final String specification) throws InvalidSpecException {
        // Whitespace is tricky to work with since it's scattered around and invisible.
        // Custom delimiters are uglier but more extensible.
        final String clean = filterWhitespace(specification);
        final String[] sections = clean.split(SECTION_DELIM);

        final String metadata = getNth(sections, 0);
        this.numRubbers = strToInt(metadata);

        final String grid = getNth(sections, 1);
        final String[] lines = grid.split(ROW_DELIM);
        final int numRows = lines.length;
        final int numCols = getNth(lines, 0).split(CELL_DELIM).length;
        final ArrayList<Ordered> prechecks = new ArrayList<>();
        final boolean[] cells = new boolean[numRows * numCols];
        for (int i = 0; i < lines.length; i++) {
            final String[] line = lines[i].split(CELL_DELIM);
            if (line.length != numCols) {
                throw new InvalidSpecException(); // <- Enforces rectangle.
            }
            for (int j = 0; j < line.length; j++) {
                final String cell = line[j];
                switch (cell) {
                    case BRICK -> {
                        cells[i * numCols + j] = true;
                    }
                    case EMPTY -> {
                        // no-op: isBrick initialized to all false.
                    }
                    default -> {
                        final int order = strToInt(cell);
                        prechecks.add(new Ordered(Grid.pack(i, j), order));
                    }
                }
            }
        }
        this.isBrick = new Grid(cells, numRows, numCols);

        // Pathery allows multiple checkpoints with same priority, but it's uncommon,
        // and harder to write a snake for, so we'll allow only one.
        throwIfNotUniqueOrder(prechecks);
        this.checkpoints = buildCheckpoints(prechecks);
    }

    // == Getters (please do not mutate mutable things!). ==============================

    public boolean isBrick(final int i, final int j) {
        return isBrick.getCell(i, j);
    }

    public int getBoundI() {
        return isBrick.getBoundI();
    }

    public int getBoundJ() {
        return isBrick.getBoundJ();
    }

    public int getNumRubbers() {
        return numRubbers;
    }

    public long[] getCheckpoints() {
        return checkpoints;
    }

    // == Parsing tools. ===============================================================

    private int strToInt(final String s) throws InvalidSpecException {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new InvalidSpecException();
        }
    }

    private <T> T getNth(final T[] array, final int n) throws InvalidSpecException {
        try {
            return array[n];
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidSpecException();
        }
    }

    private String filterWhitespace(final String dirty) {
        return dirty
            .codePoints()
            .filter(c -> !Character.isWhitespace(c))
            .collect(
                StringBuilder::new,
                StringBuilder::appendCodePoint,
                StringBuilder::append
            )
            .toString();
    }

    private void throwIfNotUniqueOrder(final Collection<Ordered> sequence)
        throws InvalidSpecException {
        final HashSet<Integer> seen = new HashSet<>();
        for (final Ordered item : sequence) {
            if (!seen.add(item.order())) {
                throw new InvalidSpecException();
            }
        }
    }

    private long[] buildCheckpoints(final ArrayList<Ordered> prechecks) {
        return prechecks
            .stream()
            .sorted(Comparator.comparingInt(Ordered::order))
            .mapToLong(Ordered::value)
            .toArray();
    }

    final record Ordered(long value, int order) {}
}
