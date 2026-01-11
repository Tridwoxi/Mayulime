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

    private static final String DELIM1 = ";;;";
    private static final String DELIM2 = ";;";
    private static final String DELIM3 = ";";

    private static final String BRICK = "#";
    private static final String EMPTY = ".";

    private final int numRubbers;
    private final boolean[][] isBrick;
    private final Point[] checkpoints; // checkpoints[n] is nth checkpoint.
    private final Point[] allPoints;

    // == Constructor and parser. ======================================================

    // If a specification is invalid, it is unrecoverable from the problem's point of
    // view, so it is only the caller that can handle it. It is a lot of redundant work
    // to check if a problem is valid, so I found begging for forgiveness cleaner.
    public Problem(final String specification) throws InvalidSpecException {
        // Whitespace is tricky to work with since it's scattered around and invisible.
        // Custom delimiters are uglier but more extensible because there are an
        // unlimited number of them.
        final String clean = filterWhitespace(specification);
        final String[] sections = clean.split(DELIM1);

        // Metadata: just an integer for number of rubbers.
        final String metadata = getNth(sections, 0);
        this.numRubbers = strToInt(metadata);

        // Grid: rectangular 2d array where elements are either a brick, empty, or
        // checkpoint. More features to be added...
        final String grid = getNth(sections, 1);
        final String[] lines = grid.split(DELIM2);
        final int expectedNumCols = getNth(lines, 0).length();
        final ArrayList<Ordered<Point>> prechecks = new ArrayList<>();
        this.isBrick = new boolean[lines.length][expectedNumCols];

        for (int i = 0; i < lines.length; i++) {
            final String[] line = lines[i].split(DELIM3);
            if (line.length != expectedNumCols) {
                throw new InvalidSpecException(); // <- Enforces rectangle.
            }
            for (int j = 0; j < line.length; j++) {
                // When more features are added, this switch should probably become an
                // if-else series because it is based on "looks like". Currently only
                // checkpoints are patterns so it works, but "force it to be a
                // checkpoint or throw" will not work for long.
                final String current = line[i];
                switch (current) {
                    case BRICK -> {
                        isBrick[i][j] = true;
                    }
                    case EMPTY -> {
                        // no-op: isBrick initialized to all false.
                    }
                    default -> {
                        final int order = strToInt(current);
                        prechecks.add(new Ordered<>(new Point(i, j), order));
                    }
                }
            }
        }
        throwIfNotUniqueOrder(prechecks);
        prechecks.sort(Comparator.comparingInt(Ordered<Point>::order));
        this.checkpoints = prechecks.toArray(Point[]::new);

        this.allPoints = new Point[getBoundI() * getBoundJ()];
        int index = 0;
        for (int i = 0; i < getBoundI(); i++) {
            for (int j = 0; j < getBoundJ(); j++) {
                allPoints[index] = new Point(i, j);
                index += 1;
            }
        }
    }

    // == Getters (please do not mutate mutable things!). ==============================

    public boolean isBrick(final Point point) {
        return !contains(point) || isBrick[point.i()][point.j()];
    }

    public boolean contains(final Point point) {
        return (
            point.i() >= 0 &&
            point.i() < getBoundI() &&
            point.j() >= 0 &&
            point.j() < getBoundJ()
        );
    }

    public int getBoundI() {
        return isBrick.length;
    }

    public int getBoundJ() {
        return isBrick[0].length;
    }

    public int getNumRubbers() {
        return numRubbers;
    }

    public Point[] getCheckpoints() {
        return checkpoints;
    }

    public Point[] getAllPoints() {
        return allPoints;
    }

    // == Parsing tools. ===============================================================

    private int strToInt(String s) throws InvalidSpecException {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new InvalidSpecException();
        }
    }

    private <T> T getNth(T[] array, int n) throws InvalidSpecException {
        try {
            return array[n];
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidSpecException();
        }
    }

    private String filterWhitespace(String dirty) {
        return dirty
            .chars()
            .filter(c -> !Character.isWhitespace(c))
            .collect(
                StringBuilder::new,
                StringBuilder::appendCodePoint,
                StringBuilder::append
            )
            .toString();
    }

    private <T> void throwIfNotUniqueOrder(Collection<Ordered<T>> sequence)
        throws InvalidSpecException {
        HashSet<Integer> seen = new HashSet<>();
        for (Ordered<T> item : sequence) {
            if (!seen.add(item.order())) {
                throw new InvalidSpecException();
            }
        }
    }

    final record Ordered<T>(T data, int order) {}
}
