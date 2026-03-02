package think.domain.repr;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import think.graph.impl.GridGraph.Cell;

/**
    Pathery problem specification. Contains metadata and empty board supplier. The {@code
    getOriginally*} methods return cells in grid traversal order.
 */
public final class Puzzle {

    private final String name;
    private final int numRows;
    private final int numCols;
    private final Board original;
    private final List<Cell> allPossibleCells;
    private final List<Cell> checkpoints;
    private final SortedSet<Cell> originallyEmpty;
    private final SortedSet<Cell> originallyMissing;
    private final SortedSet<Cell> originallyCheckpoint;
    private final int wallBudget;

    public Puzzle(
        final String name,
        final int numRows,
        final int numCols,
        final SortedSet<Cell> originallyEmpty,
        final SortedSet<Cell> originallyMissing,
        final SortedSet<Cell> originallyCheckpoint,
        final List<Cell> checkpoints,
        final int wallBudget
    ) {
        if (numRows <= 0 || numCols <= 0) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.numRows = numRows;
        this.numCols = numCols;
        this.allPossibleCells = createAllPossibleCells(numRows, numCols);
        this.originallyEmpty = new TreeSet<>(originallyEmpty);
        this.originallyMissing = new TreeSet<>(originallyMissing);
        this.originallyCheckpoint = new TreeSet<>(originallyCheckpoint);
        this.checkpoints = new ArrayList<>(checkpoints);
        this.wallBudget = Math.min(wallBudget, this.originallyEmpty.size());
        this.original = new Board(this);
    }

    public String getName() {
        return name;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public Board getBoard() {
        return original.shallowCopy();
    }

    public List<Cell> getAllPossibleCells() {
        return new ArrayList<>(allPossibleCells);
    }

    public List<Cell> getCheckpoints() {
        return new ArrayList<>(checkpoints);
    }

    public int getWallBudget() {
        return wallBudget;
    }

    public SortedSet<Cell> getOriginallyEmpty() {
        return new TreeSet<>(originallyEmpty);
    }

    public SortedSet<Cell> getOriginallyMissing() {
        return new TreeSet<>(originallyMissing);
    }

    public SortedSet<Cell> getOriginallyCheckpoint() {
        return new TreeSet<>(originallyCheckpoint);
    }

    public boolean isValid(final Board board) {
        return this == board.getPuzzle() && board.getNumSpentWalls() <= wallBudget;
    }

    private static List<Cell> createAllPossibleCells(final int numRows, final int numCols) {
        final ArrayList<Cell> cells = new ArrayList<>(numRows * numCols);
        for (int row = 0; row < numRows; row += 1) {
            for (int col = 0; col < numCols; col += 1) {
                cells.add(new Cell(row, col));
            }
        }
        return cells;
    }
}
