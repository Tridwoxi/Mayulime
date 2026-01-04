package think.repr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import think.repr.Cell.CellType;

/**
    Rectangular grid of cells indexed by natural Java (i, j). Somewhat heavy. Serves as
    problem specification.
 */
public final class Board {

    private final Cell[][] cells;
    private final int rubberSupply;
    private final HashMap<Point, Point> teleports; // <TeleportFrom, TeleportTo>.
    // Sorted by association in ascending order.
    private final ArrayList<HashSet<Point>> checkpoints;
    private final ArrayList<Point> everything;

    public Board(final Cell[][] cells, int rubberSupply)
        throws IllegalArgumentException {
        this.cells = Objects.requireNonNull(cells);
        this.rubberSupply = rubberSupply;
        this.teleports = new HashMap<>();
        this.checkpoints = new ArrayList<>();
        this.everything = new ArrayList<>();
        initialize();
    }

    public Cell getCell(final Point point) {
        final int i = point.i();
        final int j = point.j();
        if (!contains(point)) {
            throw new IllegalArgumentException("Need coordiantes in bounds.");
        }
        return cells[i][j];
    }

    public boolean contains(final Point point) {
        return (
            point.i() >= 0 &&
            point.i() < getBoundI() &&
            point.j() >= 0 &&
            point.j() < getBoundJ()
        );
    }

    public Point getDestination(final Point point) {
        if (getCell(point).type() != CellType.TELEPORT_IN) {
            throw new IllegalArgumentException("Need teleport from in cell.");
        }
        return teleports.get(point);
    }

    public ArrayList<HashSet<Point>> getCheckpoints() {
        return checkpoints;
    }

    public int getBoundI() {
        return cells.length;
    }

    public int getBoundJ() {
        return cells[0].length;
    }

    public int getRubberSupply() {
        return rubberSupply;
    }

    public ArrayList<Point> getEverything() {
        return everything;
    }

    private void initialize() {
        if (cells.length == 0 || cells[0].length == 0) {
            throw new IllegalArgumentException("Need non-empty array.");
        }
        // <Association, Teleport location / Checkpoint locations>.
        final HashMap<Integer, Point> teleIn = new HashMap<>();
        final HashMap<Integer, Point> teleOut = new HashMap<>();
        final HashMap<Integer, HashSet<Point>> checks = new HashMap<>();
        everything.ensureCapacity(getBoundI() * getBoundJ());
        for (int i = 0; i < getBoundI(); i++) {
            if (cells[i].length != getBoundJ()) {
                throw new IllegalArgumentException("Need rectangular array.");
            }
            for (int j = 0; j < getBoundJ(); j++) {
                everything.add(new Point(i, j));
            }
        }
        for (Point point : everything) {
            final Cell cell = cells[point.i()][point.j()];
            switch (cell.type()) {
                case CHECKPOINT -> {
                    if (!checks.containsKey(cell.association())) {
                        checks.put(cell.association(), new HashSet<>());
                    }
                    if (!checks.get(cell.association()).add(point)) {
                        throw new IllegalArgumentException("Need uniques.");
                    }
                }
                case TELEPORT_IN -> {
                    if (teleIn.put(cell.association(), point) != null) {
                        throw new IllegalArgumentException("Need uniques.");
                    }
                }
                case TELEPORT_OUT -> {
                    if (teleOut.put(cell.association(), point) != null) {
                        throw new IllegalArgumentException("Need uniques.");
                    }
                }
                default -> {}
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
        ArrayList<Entry<Integer, HashSet<Point>>> entries = new ArrayList<>(
            checks.entrySet()
        );
        entries.sort(Entry.comparingByKey());
        checkpoints.ensureCapacity(checks.size());
        for (Entry<Integer, HashSet<Point>> e : entries) {
            checkpoints.add(e.getValue());
        }
        if (rubberSupply < 0) {
            throw new IllegalArgumentException("Need non-negative number of rubbers.");
        }
    }
}
