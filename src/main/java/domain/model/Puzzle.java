package domain.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import solvers.graph.impl.GridGraph.Cell;

/**
    Pathery problem specification. Contains metadata, shared data, and empty board supplier.
 */
public final class Puzzle {

    private final String name;
    private final int numRows;
    private final int numCols;
    private final Board original;
    private final List<Cell> allPossibleCells;
    private final List<Cell> checkpointOrder;
    private final Set<Cell> originallyEmpty;
    private final Set<Cell> originallyMissing;
    private final Set<Cell> originallyCheckpoint;
    private final int wallBudget;

    public Puzzle(
        final String name,
        final int numRows,
        final int numCols,
        final Set<Cell> originallyEmpty,
        final Set<Cell> originallyMissing,
        final Set<Cell> originallyCheckpoint,
        final List<Cell> checkpointOrder,
        final int wallBudget
    ) {
        if (numRows <= 0 || numCols <= 0) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.numRows = numRows;
        this.numCols = numCols;
        this.allPossibleCells = createAllPossibleCells(numRows, numCols);
        this.originallyEmpty = new HashSet<>(originallyEmpty);
        this.originallyMissing = new HashSet<>(originallyMissing);
        this.originallyCheckpoint = new HashSet<>(originallyCheckpoint);
        this.checkpointOrder = new ArrayList<>(checkpointOrder);
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

    public List<Cell> getCheckpointOrder() {
        return new ArrayList<>(checkpointOrder);
    }

    public int getWallBudget() {
        return wallBudget;
    }

    public Set<Cell> getOriginallyEmpty() {
        return new HashSet<>(originallyEmpty);
    }

    public Set<Cell> getOriginallyMissing() {
        return new HashSet<>(originallyMissing);
    }

    public Set<Cell> getOriginallyCheckpoint() {
        return new HashSet<>(originallyCheckpoint);
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
