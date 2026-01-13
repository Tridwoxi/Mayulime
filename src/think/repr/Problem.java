package think.repr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import think.ana.Tools;
import think.ana.Tools.Ordered;

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
        this.numRubbers = strToInt(metadata);

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
        final ArrayList<Ordered<Cell>> prechecks = new ArrayList<>();
        final int numCells = numRows * numCols;
        final ArrayList<Boolean> cells = Tools.fill(false, numCells);
        this.emptyCells = new HashSet<>();

        for (int i = 0; i < lines.size(); i++) {
            final ArrayList<String> row = lines.get(i);
            for (int j = 0; j < row.size(); j++) {
                final String cell = row.get(j);
                switch (cell) {
                    case BRICK -> {
                        cells.set(i * numCols + j, true);
                    }
                    case EMPTY -> {
                        emptyCells.add(new Cell(i, j));
                    }
                    default -> {
                        final int order = strToInt(cell);
                        prechecks.add(new Ordered<>(new Cell(i, j), order));
                    }
                }
            }
        }
        this.isBrick = new Grid<>(cells, numRows, numCols);
        this.allCells = buildAllCells(numRows, numCols);

        // Pathery allows multiple checkpoints with same priority, but it's uncommon,
        // and harder to write a snake for, so we'll allow only one.
        throwIfNotUniqueOrder(prechecks);
        this.checkpoints = buildCheckpoints(prechecks);

        // Pathery also allows more rubbers than possible, since the player may choose
        // to not assign rubbers. But I will require a full assignment.
        final int numBricks = (int) cells
            .stream()
            .filter(b -> b)
            .count();
        final int numCheckpoints = checkpoints.size();
        if (numBricks + numCheckpoints + numRubbers > numCells) {
            throw new InvalidSpecException();
        }
    }

    // == Getters ======================================================================

    public boolean isBrick(Cell cell) {
        return isBrick.getCell(cell);
    }

    public boolean containsCell(final Cell cell) {
        return isBrick.containsCell(cell);
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

    private <T> void throwIfNotUniqueOrder(final Collection<Ordered<T>> sequence)
        throws InvalidSpecException {
        final HashSet<Integer> seen = new HashSet<>();
        for (final Ordered<T> item : sequence) {
            if (!seen.add(item.priority())) {
                throw new InvalidSpecException();
            }
        }
    }

    private <T> ArrayList<T> buildCheckpoints(final ArrayList<Ordered<T>> prechecks) {
        return prechecks
            .stream()
            .sorted(Comparator.comparingInt(Ordered::priority))
            .map(Ordered::item)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<Cell> buildAllCells(final int boundI, final int boundJ) {
        final ArrayList<Cell> cells = new ArrayList<>(boundI * boundJ);
        for (int i = 0; i < boundI; i++) {
            for (int j = 0; j < boundJ; j++) {
                cells.add(new Cell(i, j));
            }
        }
        assert cells.size() == boundI * boundJ;
        return cells;
    }

    private ArrayList<String> splitToList(final String input, final String delimiter) {
        return Pattern.compile(Pattern.quote(delimiter))
            .splitAsStream(input)
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
