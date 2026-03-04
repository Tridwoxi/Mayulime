package domain.model;

/**
    Array-backed Pathery maze. A Cell can be converted to an index of the backing grid using the
    formula {@code cell.row() * maze.getNumCols() + cell.col()}
 */
public final class Maze {

    public enum Feature {
        NOTHING(true),
        PLAYER_WALL(false),
        SYSTEM_WALL(false),
        CHECKPOINT(true);

        private final boolean passable;

        Feature(final boolean passable) {
            this.passable = passable;
        }

        public boolean isPassable() {
            return passable;
        }

        public boolean isBlocked() {
            return !passable;
        }
    }

    public record Cell(int row, int col) {
        public Cell {
            if (row < 0 || col < 0) {
                throw new IllegalArgumentException();
            }
        }
    }

    private final Feature[] grid;
    private final int numRows;
    private final int numCols;

    public Maze(final Feature[] grid, final int numRows, final int numCols) {
        if (grid.length != numRows * numCols) {
            throw new IllegalArgumentException();
        }
        this.grid = grid.clone();
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public Feature getFeature(final int index) {
        return grid[index];
    }

    public Feature getFeature(final Cell cell) {
        return grid[cell.row * numCols + cell.col];
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public Feature[] getGrid() {
        return grid.clone();
    }
}
