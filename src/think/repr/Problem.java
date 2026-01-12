package think.repr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private final Grid<Boolean> isBrick;
    private final ArrayList<Point> checkpoints;
    private final ArrayList<Point> allPoints;

    // == Constructor and parser. ======================================================

    public Problem(final String specification) throws InvalidSpecException {
        // Whitespace is tricky to work with since it's scattered around and invisible.
        // Custom delimiters are uglier but more extensible.
        final String clean = filterWhitespace(specification);
        final ArrayList<String> sections = splitToList(clean, SECTION_DELIM);

        final String metadata = getNth(sections, 0);
        this.numRubbers = strToInt(metadata);

        final String grid = getNth(sections, 1);
        final ArrayList<String> lines = splitToList(grid, ROW_DELIM);
        final int numRows = lines.size();
        final int numCols = splitToList(getNth(lines, 0), CELL_DELIM).size();
        final ArrayList<Ordered> prechecks = new ArrayList<>();
        final int numCells = numRows * numCols;
        final ArrayList<Boolean> cells = new ArrayList<>(numCells);
        for (int index = 0; index < numCells; index++) {
            cells.add(false);
        }
        for (int i = 0; i < lines.size(); i++) {
            final ArrayList<String> line = splitToList(lines.get(i), CELL_DELIM);
            if (line.size() != numCols) {
                throw new InvalidSpecException(); // <- Enforces rectangle.
            }
            for (int j = 0; j < line.size(); j++) {
                final String cell = line.get(j);
                switch (cell) {
                    case BRICK -> {
                        cells.set(i * numCols + j, true);
                    }
                    case EMPTY -> {
                        // no-op: isBrick initialized to all false.
                    }
                    default -> {
                        final int order = strToInt(cell);
                        prechecks.add(new Ordered(new Point(i, j), order));
                    }
                }
            }
        }
        this.isBrick = new Grid<>(cells, numRows, numCols);
        this.allPoints = buildAllPoints(numRows, numCols);

        // Pathery allows multiple checkpoints with same priority, but it's uncommon,
        // and harder to write a snake for, so we'll allow only one.
        throwIfNotUniqueOrder(prechecks);
        this.checkpoints = buildCheckpoints(prechecks);
    }

    // == Getters (please do not mutate mutable things!). ==============================

    public boolean isBrick(Point point) {
        return isBrick.getCell(point);
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

    public ArrayList<Point> getCheckpoints() {
        return new ArrayList<>(checkpoints);
    }

    public ArrayList<Point> getAllPoints() {
        return new ArrayList<>(allPoints);
    }

    // == Parsing tools. ===============================================================

    private int strToInt(final String s) throws InvalidSpecException {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new InvalidSpecException();
        }
    }

    private <T> T getNth(final ArrayList<T> list, final int n)
        throws InvalidSpecException {
        try {
            return list.get(n);
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

    private ArrayList<Point> buildCheckpoints(final ArrayList<Ordered> prechecks) {
        return prechecks
            .stream()
            .sorted(Comparator.comparingInt(Ordered::order))
            .map(Ordered::value)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<Point> buildAllPoints(final int boundI, final int boundJ) {
        final ArrayList<Point> points = new ArrayList<>(boundI * boundJ);
        for (int i = 0; i < boundI; i++) {
            for (int j = 0; j < boundJ; j++) {
                points.add(new Point(i, j));
            }
        }
        assert points.size() == boundI * boundJ;
        return points;
    }

    private ArrayList<String> splitToList(final String input, final String delimiter) {
        return Pattern.compile(Pattern.quote(delimiter))
            .splitAsStream(input)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    final record Ordered(Point value, int order) {}
}
