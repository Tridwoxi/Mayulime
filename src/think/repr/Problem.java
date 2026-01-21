package think.repr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import think.ana.Tools;
import think.ana.Tools.UniOrdered;

/**
    Simplified Pathery problem specification.
 */
public final class Problem {

    public static final class InvalidSpecException extends Exception {}

    private static final String SECTION_DELIM = ";;;";
    private static final String ROW_DELIM = ";;";
    private static final String CELL_DELIM = ";";
    private static final String SYSTEM_WALL = "#";
    private static final String EMPTY = ".";

    private final int numPlayerWalls;
    private final Grid<Boolean> isSystemWall;
    private final ArrayList<Cell> checkpoints;
    private final ArrayList<Cell> allCells;
    private final HashSet<Cell> emptyCells;

    // == Constructor and parser. ======================================================

    public Problem(final String specification) throws InvalidSpecException {
        // Whitespace is tricky to work with since it's scattered around and invisible.
        // Custom delimiters are uglier but more extensible.
        final String clean = filterWhitespace(specification);
        final ArrayList<String> sections = splitToList(clean, SECTION_DELIM);

        final String metadata = getNth(sections, 0);
        this.numPlayerWalls = strToInt(metadata);

        final String grid = getNth(sections, 1);
        final ArrayList<ArrayList<String>> lines = splitToList(grid, ROW_DELIM)
            .stream()
            .map(line -> splitToList(line, CELL_DELIM))
            .collect(Collectors.toCollection(ArrayList::new));
        if (!Tools.rectangular(lines)) {
            throw new InvalidSpecException();
        }
        final int numRows = lines.size();
        final int numCols = getNth(lines, 0).size();
        final ArrayList<UniOrdered<Cell>> prechecks = new ArrayList<>();
        final int numCells = numRows * numCols;
        final ArrayList<Boolean> cells = Tools.fill(false, numCells);
        this.emptyCells = new HashSet<>();

        for (int row = 0; row < lines.size(); row++) {
            final ArrayList<String> line = lines.get(row);
            for (int col = 0; col < line.size(); col++) {
                final String cell = line.get(col);
                switch (cell) {
                    case SYSTEM_WALL -> {
                        cells.set(row * numCols + col, true);
                    }
                    case EMPTY -> {
                        emptyCells.add(new Cell(row, col));
                    }
                    default -> {
                        final int order = strToInt(cell);
                        prechecks.add(new UniOrdered<>(new Cell(row, col), order));
                    }
                }
            }
        }
        this.isSystemWall = new Grid<>(cells, numRows, numCols);
        this.allCells = buildAllCells(numRows, numCols);

        // Pathery allows multiple checkpoints with same order, but it's uncommon, and
        // harder to write a snake for, so we'll allow only one.
        throwIfNotUniqueOrder(prechecks);
        this.checkpoints = buildChecks(prechecks);

        // Pathery also allows more player walls than possible, since the player may
        // choose to not assign player walls. But I will require a full assignment.
        final int numSystemWalls = (int) cells
            .stream()
            .filter(value -> value)
            .count();
        final int numCheckpoints = checkpoints.size();
        if (numSystemWalls + numCheckpoints + numPlayerWalls > numCells) {
            throw new InvalidSpecException();
        }
    }

    // == Getters ======================================================================

    public boolean isSystemWall(final Cell cell) {
        return isSystemWall.getCell(cell);
    }

    public boolean containsCell(final Cell cell) {
        return isSystemWall.containsCell(cell);
    }

    public int getRowBound() {
        return isSystemWall.getRowBound();
    }

    public int getColBound() {
        return isSystemWall.getColBound();
    }

    public int getNumPlayerWalls() {
        return numPlayerWalls;
    }

    public ArrayList<Cell> getCheckpoints() {
        // Possible optimization: if nobody wants to mutate, then return it directly.
        return new ArrayList<>(checkpoints);
    }

    public ArrayList<Cell> getAllCells() {
        return new ArrayList<>(allCells);
    }

    public HashSet<Cell> getEmptyCells() {
        return new HashSet<>(emptyCells);
    }

    // == Parsing tools. ===============================================================

    private int strToInt(final String string) throws InvalidSpecException {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException exception) {
            throw new InvalidSpecException();
        }
    }

    private <T> T getNth(final ArrayList<T> list, final int nth)
        throws InvalidSpecException {
        try {
            return list.get(nth);
        } catch (IndexOutOfBoundsException exception) {
            throw new InvalidSpecException();
        }
    }

    private String filterWhitespace(final String dirty) {
        return dirty
            .codePoints()
            .filter(character -> !Character.isWhitespace(character))
            .collect(
                StringBuilder::new,
                StringBuilder::appendCodePoint,
                StringBuilder::append
            )
            .toString();
    }

    private <T> void throwIfNotUniqueOrder(final Collection<UniOrdered<T>> sequence)
        throws InvalidSpecException {
        final HashSet<Integer> seen = new HashSet<>();
        for (final UniOrdered<T> item : sequence) {
            if (!seen.add(item.order1())) {
                throw new InvalidSpecException();
            }
        }
    }

    private <T> ArrayList<T> buildChecks(final ArrayList<UniOrdered<T>> prechecks) {
        return prechecks
            .stream()
            .sorted()
            .map(UniOrdered::item)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<Cell> buildAllCells(final int rowBound, final int colBound) {
        final ArrayList<Cell> cells = new ArrayList<>(rowBound * colBound);
        for (int row = 0; row < rowBound; row++) {
            for (int col = 0; col < colBound; col++) {
                cells.add(new Cell(row, col));
            }
        }
        assert cells.size() == rowBound * colBound;
        return cells;
    }

    private ArrayList<String> splitToList(final String input, final String delimiter) {
        return Pattern.compile(Pattern.quote(delimiter))
            .splitAsStream(input)
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
