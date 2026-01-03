package think.representation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import think.representation.Cell.CellType;

/**
    Rectangular grid of cells indexed by natural Java (i, j).
 */
public class Board {

    private static final record Point(int i, int j) {}

    private final Cell[][] cells;
    private final HashMap<Point, Point> teleports; // <TeleportFrom, TeleportTo>
    private final ArrayList<Point> checkpointList; // In sorted order.
    private final ArrayList<Point> startList;

    public Board(Cell[][] cells) {
        this.cells = Objects.requireNonNull(cells);
        this.teleports = new HashMap<>();
        this.checkpointList = new ArrayList<>();
        this.startList = new ArrayList<>();
        initialize();
    }

    public Cell getCell(int i, int j) {
        if (i < 0 || i >= getBoundI() || j < 0 || j >= getBoundJ()) {
            throw new IllegalArgumentException("Need coordiantes in bounds.");
        }
        return cells[i][j];
    }

    public Point getDestination(int i, int j) {
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
        HashMap<Integer, Point> outs = new HashMap<>(); // <Association, Teleport>
        HashMap<Integer, Point> ins = new HashMap<>(); // <Association, Teleport>
        HashMap<Integer, Point> checks = new HashMap<>(); // <Association, Checkpoint>
        for (int i = 0; i < getBoundI(); i++) {
            if (cells[i] == null || cells[i].length != getBoundJ()) {
                throw new IllegalArgumentException("Need rectangular array.");
            }
            for (int j = 0; j < getBoundJ(); j++) {
                Cell cell = cells[i][j];
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
                        if (ins.put(cell.association(), new Point(i, j)) != null) {
                            throw new IllegalArgumentException("Need uniques.");
                        }
                    }
                    case TELEPORT_OUT -> {
                        if (outs.put(cell.association(), new Point(i, j)) != null) {
                            throw new IllegalArgumentException("Need uniques.");
                        }
                    }
                    default -> {}
                }
            }
        }
        if (ins.size() != outs.size()) {
            throw new IllegalArgumentException("Need teleports of equal count.");
        }
        for (int key : ins.keySet()) {
            if (!outs.containsKey(key)) {
                throw new IllegalArgumentException("Need teleports to be paired.");
            }
            teleports.put(ins.get(key), outs.get(key));
        }
        checkpointList.addAll(checks.values());
        checkpointList.sort(
            Comparator.comparingInt(p -> getCell(p.i(), p.j()).association())
        );
        checkpointList.trimToSize();
        startList.trimToSize();
    }
}
