package think.manager;

import think.domain.model.Feature;
import think.domain.model.Puzzle;

/**
    Backend to frontend adapter.
 */
public final class StatusUpdate {

    private final String submitter;
    private final Feature[] grid;
    private final int numRows;
    private final int numCols;
    private final int score;
    private final int blockingBudget;

    StatusUpdate(
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

    public Feature getFeature(final int row, final int col) {
        return grid[row * numCols + col];
    }

    public int getScore() {
        return score;
    }

    public int getBlockingBudget() {
        return blockingBudget;
    }
}
