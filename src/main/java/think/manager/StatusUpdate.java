package think.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import think.domain.model.Maze;
import think.domain.model.Maze.Feature;
import think.domain.model.Puzzle;

/**
    Backend to frontend adapter.
 */
public final class StatusUpdate {

    private final String submitter;
    private final List<Feature> grid;
    private final int numRows;
    private final int numCols;
    private final int score;
    private final int blockingBudget;

    StatusUpdate(final String submitter, final Puzzle puzzle, final Maze maze, final int score) {
        this.submitter = submitter;
        this.grid = new ArrayList<>(Arrays.asList(maze.getGrid()));
        this.numRows = maze.getNumRows();
        this.numCols = maze.getNumCols();
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
        return grid.get(row * numCols + col);
    }

    public int getScore() {
        return score;
    }

    public int getBlockingBudget() {
        return blockingBudget;
    }
}
