package think.solvers.random;

import domain.model.Maze;
import domain.model.Maze.Feature;
import domain.model.Puzzle;
import think.common.IntArrays;
import think.solvers.Solver;

public final class RandomSolver extends Solver {

    private final int[] emptyCellIndices;
    private final RestrictedBinomial wallDistribution;
    private final int numRows;
    private final int numCols;

    public RandomSolver(final ProposedSolution listener, final Puzzle puzzle) {
        super(listener, puzzle);
        final Maze maze = puzzle.getMaze();
        this.emptyCellIndices = getEmptyCellIndices(maze.getGrid());
        this.wallDistribution = new RestrictedBinomial(
            emptyCellIndices.length,
            puzzle.getBlockingBudget()
        );
        this.numRows = maze.getNumRows();
        this.numCols = maze.getNumCols();
    }

    @Override
    protected void solve() throws KilledException {
        while (true) {
            checkAlive();
            propose(generateRandomSolution());
        }
    }

    private Maze generateRandomSolution() {
        final Feature[] grid = getPuzzle().getMaze().getGrid();
        IntArrays.shuffleInPlace(emptyCellIndices);

        final int numWalls = wallDistribution.sample();
        for (int placement = 0; placement < numWalls; placement += 1) {
            final int cell = emptyCellIndices[placement];
            grid[cell] = Feature.PLAYER_WALL;
        }
        return new Maze(grid, numRows, numCols);
    }

    private static int[] getEmptyCellIndices(final Feature[] features) {
        final int[] emptyCellIndices = new int[features.length];
        for (int index = 0; index < features.length; index += 1) {
            emptyCellIndices[index] = features[index] == Feature.BLANK ? index : -1;
        }
        return IntArrays.filteredCopy(emptyCellIndices, -1);
    }
}
