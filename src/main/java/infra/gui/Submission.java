package infra.gui;

import java.util.Arrays;
import think.domain.model.Feature;
import think.domain.model.Puzzle;

/**
    Snapshot of a solver proposal prepared for GUI rendering.
 */
public final class Submission {

    private static final int NOT_A_CHECKPOINT = -1;

    private final String submitter;
    private final Feature[] grid;
    private final int[] checkpointOrderByIndex;
    private final int numRows;
    private final int numCols;
    private final int score;
    private final int blockingBudget;

    public Submission(
        final String submitter,
        final Puzzle puzzle,
        final Feature[] features,
        final int score
    ) {
        this.submitter = submitter;
        this.grid = features.clone();
        this.numRows = puzzle.getNumRows();
        this.numCols = puzzle.getNumCols();
        this.score = score;
        this.blockingBudget = puzzle.getBlockingBudget();
        this.checkpointOrderByIndex = buildCheckpointOrderByIndex(puzzle, this.grid.length);
    }

    public String getSubmitter() {
        return submitter;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public Feature[] getFeatures() {
        return grid.clone();
    }

    public Feature getFeature(final int row, final int col) {
        return grid[row * numCols + col];
    }

    public int getCheckpointOrder(final int row, final int col) {
        return checkpointOrderByIndex[row * numCols + col];
    }

    public int getScore() {
        return score;
    }

    public int getBlockingBudget() {
        return blockingBudget;
    }

    private static int[] buildCheckpointOrderByIndex(final Puzzle puzzle, final int numCells) {
        final int[] orderByIndex = new int[numCells];
        Arrays.fill(orderByIndex, NOT_A_CHECKPOINT);

        final int[] checkpoints = puzzle.getCheckpoints();
        for (int order = 0; order < checkpoints.length; order += 1) {
            orderByIndex[checkpoints[order]] = order;
        }
        return orderByIndex;
    }
}
