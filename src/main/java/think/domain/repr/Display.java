package think.domain.repr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import think.graph.impl.GridGraph;
import think.graph.impl.GridGraph.Cell;

/**
    Immutable display adapter to allow for simpler querying of backend results.
 */
public final class Display {

    public enum Kind {
        EMPTY,
        CHECKPOINT,
        SYSTEM_WALL,
        PLAYER_WALL,
    }

    private static final String CHECKPOINT_PREFIX = "c";
    private static final String UNNAMED = "";

    private final String submitter;
    private final int score;
    private final String puzzleName;
    private final int numRows;
    private final int numCols;
    private final int wallBudget;
    private final int spentWallsCount;
    private final List<Cell> cells;
    private final Map<Cell, Kind> kindsByCell;
    private final Map<Cell, String> namesByCell;

    public Display(
        final String submitter,
        final Puzzle puzzle,
        final Board board,
        final int score
    ) {
        this.submitter = submitter;
        this.score = score;
        this.puzzleName = puzzle.getName();
        this.wallBudget = puzzle.getWallBudget();
        this.spentWallsCount = board.getNumSpentWalls();
        this.cells = puzzle.getAllPossibleCells();
        this.numRows = calculateNumRows(cells) + 1;
        this.numCols = calculateNumCols(cells) + 1;
        this.kindsByCell = new HashMap<>(cells.size());
        this.namesByCell = new HashMap<>(cells.size());

        final GridGraph<Object> currentGraph = board.getTraversalGraph();
        final Set<Cell> originallyMissing = new HashSet<>(puzzle.getOriginallyMissing());
        final Set<Cell> originallyCheckpoint = new HashSet<>(puzzle.getOriginallyCheckpoint());
        final Map<Cell, Integer> checkpointOrder = new HashMap<>();
        int order = 0;
        for (final Cell checkpoint : puzzle.getCheckpointOrder()) {
            checkpointOrder.put(checkpoint, order);
            order += 1;
        }
        for (final Cell cell : cells) {
            kindsByCell.put(
                cell,
                determineKind(cell, originallyMissing, originallyCheckpoint, currentGraph)
            );
            namesByCell.put(cell, determineName(cell, checkpointOrder));
        }
    }

    public List<Cell> getAllCells() {
        return new ArrayList<>(cells);
    }

    public String getSubmitter() {
        return submitter;
    }

    public int getScore() {
        return score;
    }

    public String getPuzzleName() {
        return puzzleName;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public int getWallBudget() {
        return wallBudget;
    }

    public int getSpentWallsCount() {
        return spentWallsCount;
    }

    public Kind getKind(final Cell cell) {
        if (!kindsByCell.containsKey(cell)) {
            throw new IllegalArgumentException();
        }
        return kindsByCell.get(cell);
    }

    public String getName(final Cell cell) {
        if (!namesByCell.containsKey(cell)) {
            throw new IllegalArgumentException();
        }
        return namesByCell.get(cell);
    }

    private static Kind determineKind(
        final Cell cell,
        final Set<Cell> originallyMissing,
        final Set<Cell> originallyCheckpoint,
        final GridGraph<Object> currentGraph
    ) {
        if (originallyMissing.contains(cell)) {
            return Kind.SYSTEM_WALL;
        }
        if (originallyCheckpoint.contains(cell)) {
            return Kind.CHECKPOINT;
        }
        if (!currentGraph.containsVertexKey(cell)) {
            return Kind.PLAYER_WALL;
        }
        return Kind.EMPTY;
    }

    private static String determineName(final Cell cell, final Map<Cell, Integer> checkpointOrder) {
        if (!checkpointOrder.containsKey(cell)) {
            return UNNAMED;
        }
        return CHECKPOINT_PREFIX + checkpointOrder.get(cell);
    }

    private static int calculateNumRows(final List<Cell> cells) {
        return cells.stream().mapToInt(Cell::row).max().orElseThrow(IllegalStateException::new);
    }

    private static int calculateNumCols(final List<Cell> cells) {
        return cells.stream().mapToInt(Cell::col).max().orElseThrow(IllegalStateException::new);
    }
}
