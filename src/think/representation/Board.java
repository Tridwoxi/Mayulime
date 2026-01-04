package think.representation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import think.representation.Cell.CellType;

/**
    Rectangular grid of cells indexed by natural Java (i, j).
 */
public final class Board {

    private static final record Point(int i, int j) {}

    private final Cell[][] cells;
    private final int numRubbers;
    private final HashMap<Point, Point> teleports; // <TeleportFrom, TeleportTo>.
    private final ArrayList<Point> checkpointList; // Sorted by association, ascending.
    private final ArrayList<Point> startList;

    public Board(final Cell[][] cells, int numRubbers) throws IllegalArgumentException {
        this.cells = Objects.requireNonNull(cells);
        this.numRubbers = numRubbers;
        this.teleports = new HashMap<>();
        this.checkpointList = new ArrayList<>();
        this.startList = new ArrayList<>();
        initialize();
    }

    public Cell getCell(final int i, final int j) {
        if (i < 0 || i >= getBoundI() || j < 0 || j >= getBoundJ()) {
            throw new IllegalArgumentException("Need coordiantes in bounds.");
        }
        return cells[i][j];
    }

    public Point getDestination(final int i, final int j) {
        if (getCell(i, j).type() != CellType.TELEPORT_IN) {
            throw new IllegalArgumentException("Need teleport from in cell.");
        }
        return teleports.get(new Point(i, j));
    }

    public ArrayList<Point> getCheckpointList() {
        return checkpointList;
    }

    public ArrayList<Point> getStartList() {
        return startList;
    }

    public int getBoundI() {
        return cells.length;
    }

    public int getBoundJ() {
        return cells[0].length;
    }

    private void initialize() {
        if (cells.length == 0 || cells[0] == null || cells[0].length == 0) {
            throw new IllegalArgumentException("Need non-empty array.");
        }
        // <Association, Teleport/Checkpoint location>.
        final HashMap<Integer, Point> teleIn = new HashMap<>();
        final HashMap<Integer, Point> teleOut = new HashMap<>();
        final HashMap<Integer, Point> checks = new HashMap<>();
        for (int i = 0; i < getBoundI(); i++) {
            if (cells[i] == null || cells[i].length != getBoundJ()) {
                throw new IllegalArgumentException("Need rectangular array.");
            }
            for (int j = 0; j < getBoundJ(); j++) {
                final Cell cell = cells[i][j];
                switch (cell.type()) {
                    case START -> {
                        startList.add(new Point(i, j));
                    }
                    case CHECKPOINT -> {
                        if (checks.put(cell.association(), new Point(i, j)) != null) {
                            throw new IllegalArgumentException("Need uniques.");
                        }
                    }
                    case TELEPORT_IN -> {
                        if (teleIn.put(cell.association(), new Point(i, j)) != null) {
                            throw new IllegalArgumentException("Need uniques.");
                        }
                    }
                    case TELEPORT_OUT -> {
                        if (teleOut.put(cell.association(), new Point(i, j)) != null) {
                            throw new IllegalArgumentException("Need uniques.");
                        }
                    }
                    default -> {}
                }
            }
        }
        if (teleIn.size() != teleOut.size()) {
            throw new IllegalArgumentException("Need teleports of equal count.");
        }
        for (final int key : teleIn.keySet()) {
            if (!teleOut.containsKey(key)) {
                throw new IllegalArgumentException("Need teleports to be paired.");
            }
            teleports.put(teleIn.get(key), teleOut.get(key));
        }
        checkpointList.addAll(checks.values());
        checkpointList.sort(
            Comparator.comparingInt(p -> getCell(p.i(), p.j()).association())
        );
        checkpointList.trimToSize();
        startList.trimToSize();
        if (numRubbers < 0) {
            throw new IllegalArgumentException("Need non-negative number of rubbers.");
        }
    }
}
